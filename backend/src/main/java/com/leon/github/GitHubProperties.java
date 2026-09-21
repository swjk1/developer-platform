package com.leon.github;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param token    optional. Unauthenticated GitHub allows 60 requests an hour,
 *                 authenticated allows 5000. Absent is a supported mode, and
 *                 the demo does not need one.
 * @param interval how often the scheduled job runs (section 11).
 */
@ConfigurationProperties(prefix = "github")
public record GitHubProperties(
        String baseUrl,
        String token,
        Duration interval,
        Duration connectTimeout,
        Duration readTimeout,
        int maxAttempts,
        Duration retryBase,
        Duration retryMax,
        double retryJitter) {

    public boolean hasToken() {
        return token != null && !token.isBlank();
    }
}
