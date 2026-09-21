package com.leon.github;

import com.leon.github.GitHubApiException.Kind;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Talks to the GitHub REST API, and assumes it will fail (section 12).
 *
 * <p>Retries are hand-rolled rather than delegated to Spring Retry so that the
 * decision of <em>what</em> is worth retrying sits next to the decision of
 * <em>how long</em> to wait, both visible in one place. A 404 and a malformed
 * body are never retried — asking again cannot change either.
 */
@Component
public class GitHubApiClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubApiClient.class);

    private final RestClient http;
    private final GitHubProperties props;
    private final Sleeper sleeper;

    /** Injectable so the retry tests do not actually wait. */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    public GitHubApiClient(GitHubProperties props) {
        this(props, duration -> Thread.sleep(duration.toMillis()));
    }

    GitHubApiClient(GitHubProperties props, Sleeper sleeper) {
        this.props = props;
        this.sleeper = sleeper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) props.connectTimeout().toMillis());
        factory.setReadTimeout((int) props.readTimeout().toMillis());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.USER_AGENT, "developer-platform");

        if (props.hasToken()) {
            builder = builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.token());
        }
        this.http = builder.build();
    }

    /**
     * One repository's metadata.
     *
     * @throws GitHubApiException when every permitted attempt failed
     */
    public GitHubSnapshot fetch(String owner, String repository) {
        String what = owner + "/" + repository;
        GitHubPayloads.Repo repo = withRetries(() -> getRepo(owner, repository), what);

        // A repository with no releases 404s here, which is normal rather than
        // a failed sync. Every failure is swallowed: a missing release tag must
        // never cost us the star count we just successfully fetched.
        String release = null;
        try {
            GitHubPayloads.Release latest = getLatestRelease(owner, repository);
            release = latest == null ? null : latest.tagName();
        } catch (GitHubApiException noRelease) {
            log.debug("No release metadata for {}: {}", what, noRelease.kind());
        }

        return new GitHubSnapshot(
                orZero(repo.stars()),
                orZero(repo.forks()),
                orZero(repo.openIssues()),
                repo.language(),
                repo.pushedAt(),
                release);
    }

    private <T> T withRetries(Supplier<T> call, String what) {
        GitHubApiException last = null;

        for (int attempt = 1; attempt <= props.maxAttempts(); attempt++) {
            try {
                return call.get();
            } catch (GitHubApiException e) {
                last = e;
                if (!e.isRetryable() || attempt == props.maxAttempts()) {
                    throw e;
                }
                Duration wait = Backoff.delayAfter(
                        attempt,
                        props.retryBase(),
                        props.retryMax(),
                        props.retryJitter(),
                        () -> ThreadLocalRandom.current().nextDouble());
                log.warn("GitHub {} for {} (attempt {} of {}), retrying in {} ms",
                        e.kind(), what, attempt, props.maxAttempts(), wait.toMillis());
                try {
                    sleeper.sleep(wait);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new GitHubApiException(Kind.UNAVAILABLE, "Interrupted while backing off", interrupted);
                }
            }
        }
        throw last;
    }

    private GitHubPayloads.Repo getRepo(String owner, String repository) {
        return exchange("/repos/{owner}/{repo}", GitHubPayloads.Repo.class, owner, repository);
    }

    private GitHubPayloads.Release getLatestRelease(String owner, String repository) {
        return exchange("/repos/{owner}/{repo}/releases/latest", GitHubPayloads.Release.class, owner, repository);
    }

    private <T> T exchange(String path, Class<T> type, Object... uriVars) {
        try {
            return http.get()
                    .uri(path, uriVars)
                    .exchange((request, response) -> {
                        HttpStatusCode status = response.getStatusCode();
                        if (status.is2xxSuccessful()) {
                            try {
                                return response.bodyTo(type);
                            } catch (Exception malformed) {
                                throw new GitHubApiException(Kind.MALFORMED,
                                        "Could not parse GitHub response for " + path, malformed);
                            }
                        }
                        throw translate(status, response.getHeaders(), path);
                    }, false);
        } catch (ResourceAccessException networkFailure) {
            // Read timeout, connect timeout, DNS failure — all transient.
            throw new GitHubApiException(Kind.UNAVAILABLE,
                    "GitHub unreachable for " + path + ": " + networkFailure.getMessage(), networkFailure);
        }
    }

    private static GitHubApiException translate(HttpStatusCode status, HttpHeaders headers, String path) {
        int code = status.value();

        if (code == 404) {
            return new GitHubApiException(Kind.NOT_FOUND, "GitHub 404 for " + path);
        }
        if (code == 429 || (code == 403 && isRateLimited(headers))) {
            return new GitHubApiException(Kind.RATE_LIMITED, "GitHub rate limit hit for " + path);
        }
        if (code == 403) {
            // A 403 without the rate-limit marker is an authorisation problem.
            // Retrying a token that is not allowed in is pointless.
            return new GitHubApiException(Kind.NOT_FOUND, "GitHub 403, not rate limited, for " + path);
        }
        return new GitHubApiException(Kind.UNAVAILABLE, "GitHub " + code + " for " + path);
    }

    /** GitHub signals an exhausted quota with x-ratelimit-remaining: 0. */
    private static boolean isRateLimited(HttpHeaders headers) {
        return "0".equals(headers.getFirst("x-ratelimit-remaining"))
                || headers.getFirst("retry-after") != null;
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
