package com.leon.github;

/**
 * Everything the GitHub client can fail with, carrying why. The sync service
 * branches on {@link Kind}: rate limiting is recorded differently from a
 * genuine outage, and a repository that does not exist must never be retried.
 */
public class GitHubApiException extends RuntimeException {

    public enum Kind {
        /** 403 or 429 with rate-limit headers. Back off; do not hammer. */
        RATE_LIMITED,
        /** 5xx, timeout, DNS failure — transient, worth retrying. */
        UNAVAILABLE,
        /** 404. The repository is gone or renamed; retrying cannot help. */
        NOT_FOUND,
        /** 2xx whose body could not be parsed. Retrying rarely helps. */
        MALFORMED
    }

    private final Kind kind;

    public GitHubApiException(Kind kind, String message) {
        this(kind, message, null);
    }

    public GitHubApiException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    /** NOT_FOUND and MALFORMED do not get better by being asked again. */
    public boolean isRetryable() {
        return kind == Kind.UNAVAILABLE;
    }
}
