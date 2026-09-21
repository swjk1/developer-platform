package com.leon.github;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Section 9. Pulls repository metadata from GitHub into PostgreSQL.
 *
 * <p>The governing invariant (section 82) is that <b>a GitHub failure must not
 * crash the project API</b>. Nothing in here propagates an exception to a
 * caller: a failed repository records why it failed and keeps whatever was
 * last fetched, and the sync moves on to the next one. One dead repository
 * must not stop the other two from updating.
 */
@Service
public class GitHubSyncService {

    private static final Logger log = LoggerFactory.getLogger(GitHubSyncService.class);

    private final GitHubMetadataRepository repository;
    private final GitHubApiClient client;

    public GitHubSyncService(GitHubMetadataRepository repository, GitHubApiClient client) {
        this.repository = repository;
        this.client = client;
    }

    /** @return how many repositories were refreshed successfully */
    @Transactional
    public SyncReport syncAll() {
        List<GitHubRepository> all = repository.findAll();
        int succeeded = 0;
        int failed = 0;

        for (GitHubRepository repo : all) {
            if (syncOne(repo)) {
                succeeded++;
            } else {
                failed++;
            }
        }

        log.info("GitHub sync finished: {} succeeded, {} failed, {} total", succeeded, failed, all.size());
        return new SyncReport(all.size(), succeeded, failed);
    }

    private boolean syncOne(GitHubRepository repo) {
        try {
            GitHubSnapshot snapshot = client.fetch(repo.getOwner(), repo.getRepository());
            repo.recordSuccess(snapshot, Instant.now());
            log.debug("Synced {}: {} stars", repo.fullName(), snapshot.stars());
            return true;

        } catch (GitHubApiException e) {
            // Rate limiting is recorded separately from a genuine failure so a
            // dashboard can tell "we are being throttled" from "it is broken".
            SyncStatus status = e.kind() == GitHubApiException.Kind.RATE_LIMITED
                    ? SyncStatus.RATE_LIMITED
                    : SyncStatus.FAILED;
            repo.recordFailure(status);
            log.warn("GitHub sync failed for {} ({}): {}", repo.fullName(), status, e.getMessage());
            return false;

        } catch (RuntimeException unexpected) {
            // A bug in parsing or mapping should degrade this repository, not
            // abort the whole run.
            repo.recordFailure(SyncStatus.FAILED);
            log.error("Unexpected error syncing {}", repo.fullName(), unexpected);
            return false;
        }
    }

    /** Read side, used by the project module to decorate its responses. */
    @Transactional(readOnly = true)
    public Map<Long, GitHubSummary> summariesFor(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }
        return repository.findByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(GitHubRepository::getProjectId, GitHubSummary::from));
    }

    @Transactional(readOnly = true)
    public Optional<GitHubSummary> summaryFor(Long projectId) {
        return repository.findByProjectId(projectId).map(GitHubSummary::from);
    }

    public record SyncReport(int total, int succeeded, int failed) {
    }
}
