package com.leon.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.leon.github.GitHubMetadataRepository;
import com.leon.github.GitHubRepository;
import com.leon.github.GitHubSyncService;
import com.leon.github.SyncStatus;
import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Section 41. The GitHub API is mocked with WireMock rather than called, so
 * the failure matrix is exercised deterministically and the suite never
 * depends on the network or burns rate limit.
 *
 * <p>The assertion that matters in every failure case is the same one: the
 * previously fetched numbers survive. That is the invariant in section 82 —
 * a GitHub failure must not crash, or empty, the project API.
 */
class GitHubSyncIT extends IntegrationTestBase {

    private static final WireMockServer GITHUB =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        GITHUB.start();
    }

    @AfterAll
    static void stopWireMock() {
        GITHUB.stop();
    }

    @DynamicPropertySource
    static void pointClientAtWireMock(DynamicPropertyRegistry registry) {
        registry.add("github.base-url", () -> "http://localhost:" + GITHUB.port());
    }

    @Autowired
    GitHubSyncService service;

    @Autowired
    GitHubMetadataRepository repository;

    private static final String KEYGUARD = "/repos/swjk1/keyguard";

    @BeforeEach
    void reset() {
        GITHUB.resetAll();
        // Every repository except the one under test 404s, which the service
        // records as FAILED and otherwise ignores.
        GITHUB.stubFor(get(urlMatching("/repos/.*")).willReturn(aResponse().withStatus(404)));
    }

    // ---------- the happy path ----------

    @Test
    void storesMetadataFromASuccessfulResponse() {
        stubRepo(KEYGUARD, 200, """
                {
                  "stargazers_count": 42,
                  "forks_count": 7,
                  "open_issues_count": 3,
                  "language": "Kotlin",
                  "pushed_at": "2026-09-19T21:42:00Z"
                }
                """);

        service.syncAll();

        GitHubRepository saved = keyguard();
        assertThat(saved.getSyncStatus()).isEqualTo(SyncStatus.OK);
        assertThat(saved.getStars()).isEqualTo(42);
        assertThat(saved.getForks()).isEqualTo(7);
        assertThat(saved.getOpenIssues()).isEqualTo(3);
        assertThat(saved.getPrimaryLanguage()).isEqualTo("Kotlin");
        assertThat(saved.getLastCommitAt()).isEqualTo(Instant.parse("2026-09-19T21:42:00Z"));
        assertThat(saved.getLastSyncedAt()).isNotNull();
    }

    @Test
    void toleratesAResponseMissingOptionalFields() {
        // A brand new repository has no language and no releases.
        stubRepo(KEYGUARD, 200, """
                {"stargazers_count": 1, "forks_count": 0, "open_issues_count": 0, "pushed_at": null}
                """);

        service.syncAll();

        GitHubRepository saved = keyguard();
        assertThat(saved.getSyncStatus()).isEqualTo(SyncStatus.OK);
        assertThat(saved.getPrimaryLanguage()).isNull();
        assertThat(saved.getLastCommitAt()).isNull();
    }

    @Test
    void ignoresUnknownFieldsGitHubMayAdd() {
        stubRepo(KEYGUARD, 200, """
                {"stargazers_count": 5, "some_field_invented_next_year": {"nested": true}}
                """);

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.OK);
        assertThat(keyguard().getStars()).isEqualTo(5);
    }

    // ---------- the failure matrix ----------

    @Test
    void a403RateLimitIsRecordedAsRateLimitedAndKeepsStaleData() {
        givenPreviouslySyncedWith(42);

        GITHUB.stubFor(get(urlEqualTo(KEYGUARD)).willReturn(aResponse()
                .withStatus(403)
                .withHeader("x-ratelimit-remaining", "0")));

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.RATE_LIMITED);
        assertThat(keyguard().getStars()).as("stale data must survive").isEqualTo(42);
    }

    @Test
    void a429IsRecordedAsRateLimited() {
        givenPreviouslySyncedWith(42);

        GITHUB.stubFor(get(urlEqualTo(KEYGUARD)).willReturn(aResponse().withStatus(429)));

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.RATE_LIMITED);
        assertThat(keyguard().getStars()).isEqualTo(42);
    }

    @Test
    void a500IsRetriedThenRecordedAsFailed() {
        givenPreviouslySyncedWith(42);

        GITHUB.stubFor(get(urlEqualTo(KEYGUARD)).willReturn(aResponse().withStatus(500)));

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(keyguard().getStars()).isEqualTo(42);
        // max-attempts is 2 in the test profile, so a transient error is tried
        // twice before being given up on.
        GITHUB.verify(2, getRequestedFor(urlEqualTo(KEYGUARD)));
    }

    @Test
    void aTimeoutIsRecordedAsFailed() {
        givenPreviouslySyncedWith(42);

        // read-timeout is 500 ms in the test profile.
        GITHUB.stubFor(get(urlEqualTo(KEYGUARD))
                .willReturn(aResponse().withStatus(200).withFixedDelay(2000)));

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(keyguard().getStars()).isEqualTo(42);
    }

    @Test
    void malformedJsonIsRecordedAsFailedAndNotRetried() {
        givenPreviouslySyncedWith(42);

        stubRepo(KEYGUARD, 200, "{ this is not json");

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.FAILED);
        assertThat(keyguard().getStars()).isEqualTo(42);
        // Re-requesting a body that does not parse cannot help.
        GITHUB.verify(1, getRequestedFor(urlEqualTo(KEYGUARD)));
    }

    @Test
    void a404IsNotRetried() {
        GITHUB.stubFor(get(urlEqualTo(KEYGUARD)).willReturn(aResponse().withStatus(404)));

        service.syncAll();

        assertThat(keyguard().getSyncStatus()).isEqualTo(SyncStatus.FAILED);
        GITHUB.verify(1, getRequestedFor(urlEqualTo(KEYGUARD)));
    }

    @Test
    void oneBrokenRepositoryDoesNotStopTheOthers() {
        stubRepo(KEYGUARD, 200, """
                {"stargazers_count": 11}
                """);
        // The other two are left on the 404 default from reset().

        GitHubSyncService.SyncReport report = service.syncAll();

        assertThat(report.total()).isEqualTo(3);
        assertThat(report.succeeded()).isEqualTo(1);
        assertThat(report.failed()).isEqualTo(2);
        assertThat(keyguard().getStars()).isEqualTo(11);
    }

    // ---------- helpers ----------

    private void stubRepo(String path, int status, String body) {
        GITHUB.stubFor(get(urlEqualTo(path)).willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));
    }

    private void givenPreviouslySyncedWith(int stars) {
        stubRepo(KEYGUARD, 200, "{\"stargazers_count\": " + stars + "}");
        service.syncAll();
        assertThat(keyguard().getStars()).isEqualTo(stars);
        GITHUB.resetAll();
        GITHUB.stubFor(get(urlMatching("/repos/.*")).willReturn(aResponse().withStatus(404)));
    }

    private GitHubRepository keyguard() {
        return repository.findByOwnerAndRepository("swjk1", "keyguard").orElseThrow();
    }
}
