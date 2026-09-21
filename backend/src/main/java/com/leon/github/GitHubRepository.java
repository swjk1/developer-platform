package com.leon.github;

import jakarta.persistence.*;
import java.time.Instant;

/** Section 10. The mirrored metadata for one repository. */
@Entity
@Table(name = "github_repositories")
public class GitHubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String repository;

    @Column(nullable = false)
    private int stars;

    @Column(nullable = false)
    private int forks;

    @Column(name = "open_issues", nullable = false)
    private int openIssues;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Column(name = "last_commit_at")
    private Instant lastCommitAt;

    @Column(name = "last_release")
    private String lastRelease;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.PENDING;

    protected GitHubRepository() {
        // for JPA
    }

    /** "owner/repository", the form GitHub itself uses. */
    public String fullName() {
        return owner + "/" + repository;
    }

    /**
     * Applies a successful fetch. Called only on success, so a failure can
     * never partially overwrite good data with nulls.
     */
    public void recordSuccess(GitHubSnapshot snapshot, Instant at) {
        this.stars = snapshot.stars();
        this.forks = snapshot.forks();
        this.openIssues = snapshot.openIssues();
        this.primaryLanguage = snapshot.primaryLanguage();
        this.lastCommitAt = snapshot.lastCommitAt();
        this.lastRelease = snapshot.lastRelease();
        this.lastSyncedAt = at;
        this.syncStatus = SyncStatus.OK;
    }

    /**
     * Records that a sync failed. Deliberately touches nothing but the status:
     * the previously fetched numbers stay exactly as they were, which is what
     * lets the API keep answering while GitHub is down.
     */
    public void recordFailure(SyncStatus status) {
        this.syncStatus = status;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getOwner() { return owner; }
    public String getRepository() { return repository; }
    public int getStars() { return stars; }
    public int getForks() { return forks; }
    public int getOpenIssues() { return openIssues; }
    public String getPrimaryLanguage() { return primaryLanguage; }
    public Instant getLastCommitAt() { return lastCommitAt; }
    public String getLastRelease() { return lastRelease; }
    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public SyncStatus getSyncStatus() { return syncStatus; }
}
