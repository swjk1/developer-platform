package com.leon.project;

import com.leon.common.ResourceNotFoundException;
import com.leon.github.GitHubSummary;
import com.leon.github.GitHubSyncService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side of the projects module. Caching (spec section 14) wraps this class
 * rather than the controller, so the cache sits on one seam and the HTTP layer
 * stays unaware of it.
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository repository;
    private final GitHubSyncService github;

    public ProjectService(ProjectRepository repository, GitHubSyncService github) {
        this.repository = repository;
        this.github = github;
    }

    public List<ProjectResponse> findAll(ProjectStatus status, Boolean featured) {
        List<Project> found;
        if (status != null && featured != null) {
            found = repository.findByStatusAndFeaturedOrderByDisplayOrderAscIdAsc(status, featured);
        } else if (status != null) {
            found = repository.findByStatusOrderByDisplayOrderAscIdAsc(status);
        } else if (featured != null) {
            found = repository.findByFeaturedOrderByDisplayOrderAscIdAsc(featured);
        } else {
            found = repository.findAllByOrderByDisplayOrderAscIdAsc();
        }

        // One query for every project's metadata rather than one per project.
        // The list endpoint is the hot path and an N+1 here would be the first
        // thing a load test finds.
        Map<Long, GitHubSummary> summaries =
                github.summariesFor(found.stream().map(Project::getId).toList());

        return found.stream()
                .map(p -> ProjectResponse.from(p, summaries.get(p.getId())))
                .toList();
    }

    public ProjectResponse findBySlug(String slug) {
        Project project = repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", slug));

        return ProjectResponse.from(project, github.summaryFor(project.getId()).orElse(null));
    }
}
