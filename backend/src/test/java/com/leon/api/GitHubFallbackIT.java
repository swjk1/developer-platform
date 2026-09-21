package com.leon.api;

// Not a wildcard import: WireMock exports its own equalTo(String), which
// returns a StringValuePattern and silently shadows Hamcrest's matcher in a
// file that needs both.
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.leon.github.GitHubSyncService;
import com.leon.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Spec section 97, the second demo scenario, as an executable test.
 *
 * <p>GitHub goes down. The projects API must still answer 200, and it must
 * still carry the last metadata that was successfully fetched. This is the
 * test the whole retry-and-fallback design exists to satisfy.
 */
class GitHubFallbackIT extends IntegrationTestBase {

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

    @LocalServerPort
    int port;

    @Autowired
    GitHubSyncService sync;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        GITHUB.resetAll();
    }

    @Test
    void theProjectsApiKeepsServingCachedMetadataWhileGitHubIsDown() {
        // A good sync happens first, so there is something to fall back to.
        GITHUB.stubFor(get(urlEqualTo("/repos/swjk1/keyguard")).willReturn(okJson("""
                {"stargazers_count": 128, "language": "Kotlin", "pushed_at": "2026-09-20T10:00:00Z"}
                """)));
        GITHUB.stubFor(get(urlMatching("/repos/.*")).willReturn(aResponse().withStatus(404)));
        sync.syncAll();

        // Now GitHub falls over completely.
        GITHUB.resetAll();
        GITHUB.stubFor(get(urlMatching(".*")).willReturn(aResponse().withStatus(500)));
        sync.syncAll();

        // The website does not care.
        given()
        .when().get("/api/v1/projects/keyguard")
        .then().statusCode(200)
               .body("slug", equalTo("keyguard"))
               .body("github.repository", equalTo("swjk1/keyguard"))
               .body("github.stars", equalTo(128))
               .body("github.primaryLanguage", equalTo("Kotlin"));
    }

    @Test
    void theListEndpointExposesGitHubMetadataForEveryProject() {
        given()
        .when().get("/api/v1/projects")
        .then().statusCode(200)
               .body("github.repository", everyItem(notNullValue()))
               .body("findAll { it.slug == 'cookpilot' }.github.repository", hasItem("swjk1/CookBot"));
    }

    @Test
    void syncBookkeepingIsNotExposedPublicly() {
        // sync_status and last_synced_at are operator concerns. Leaking them
        // would put internal state in a public contract for no benefit.
        given()
        .when().get("/api/v1/projects/keyguard")
        .then().statusCode(200)
               .body("github.syncStatus", nullValue())
               .body("github.lastSyncedAt", nullValue());
    }
}
