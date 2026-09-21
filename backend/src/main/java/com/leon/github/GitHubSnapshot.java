package com.leon.github;

import java.time.Instant;

/**
 * What one fetch produced. Separate from the entity so the client can be
 * tested without a database, and so a half-parsed response can never be
 * handed to the persistence layer.
 */
public record GitHubSnapshot(
        int stars,
        int forks,
        int openIssues,
        String primaryLanguage,
        Instant lastCommitAt,
        String lastRelease) {
}
