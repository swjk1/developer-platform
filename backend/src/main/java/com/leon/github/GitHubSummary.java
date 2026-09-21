package com.leon.github;

import java.time.Instant;

/**
 * The read model the project module consumes — the `github` object in section
 * 6's example response. Deliberately narrower than the entity: the public API
 * has no business exposing sync bookkeeping.
 */
public record GitHubSummary(
        String repository,
        int stars,
        String primaryLanguage,
        Instant lastCommit) {

    static GitHubSummary from(GitHubRepository r) {
        return new GitHubSummary(r.fullName(), r.getStars(), r.getPrimaryLanguage(), r.getLastCommitAt());
    }
}
