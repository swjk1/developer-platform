package com.leon.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Section 11. The schedule, kept separate from {@link GitHubSyncService} so
 * the service can be tested without Spring's scheduler running underneath it.
 *
 * <p>Syncing on request would put an unpredictable third-party call on the
 * path of every page view, which is exactly what section 11 forbids. This runs
 * on its own clock and the API only ever reads what it left behind.
 */
@Component
public class GitHubSyncJob {

    private static final Logger log = LoggerFactory.getLogger(GitHubSyncJob.class);

    private final GitHubSyncService service;
    private final boolean syncOnStartup;

    public GitHubSyncJob(GitHubSyncService service,
                         @org.springframework.beans.factory.annotation.Value("${github.sync-on-startup:false}")
                         boolean syncOnStartup) {
        this.service = service;
        this.syncOnStartup = syncOnStartup;
    }

    /**
     * fixedDelay rather than fixedRate: the gap is measured from the end of
     * the previous run, so a slow sync can never overlap with the next one and
     * double the request rate against an API that is already struggling.
     */
    @Scheduled(fixedDelayString = "${github.interval}", initialDelayString = "${github.initial-delay:PT1M}")
    public void synchronizeRepositories() {
        log.debug("Scheduled GitHub sync starting");
        service.syncAll();
    }

    /**
     * Off by default. Useful on a fresh deployment where every row is PENDING
     * and waiting half an hour for the first numbers is unhelpful; unwanted in
     * tests and in CI, where it would fire real network calls.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartupIfEnabled() {
        if (syncOnStartup) {
            log.info("github.sync-on-startup is enabled, syncing now");
            service.syncAll();
        }
    }
}
