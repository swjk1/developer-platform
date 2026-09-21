package com.leon.github;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubMetadataRepository extends JpaRepository<GitHubRepository, Long> {

    Optional<GitHubRepository> findByProjectId(Long projectId);

    /** Batched on purpose: the list endpoint must not issue one query per project. */
    List<GitHubRepository> findByProjectIdIn(Collection<Long> projectIds);

    Optional<GitHubRepository> findByOwnerAndRepository(String owner, String repository);
}
