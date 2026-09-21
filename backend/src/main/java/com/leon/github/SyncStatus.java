package com.leon.github;

/**
 * The outcome of the most recent sync attempt for one repository.
 *
 * <p>PENDING is distinct from FAILED on purpose: "never fetched" and "fetched
 * and it went wrong" need different answers when deciding whether the stored
 * numbers can be shown.
 */
public enum SyncStatus {
    PENDING,
    OK,
    FAILED,
    RATE_LIMITED
}
