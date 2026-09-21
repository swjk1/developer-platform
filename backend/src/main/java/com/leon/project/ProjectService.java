package com.leon.project;

import com.leon.common.ResourceNotFoundException;
import java.util.List;
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

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
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
        return found.stream().map(ProjectResponse::from).toList();
    }

    public ProjectResponse findBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(ProjectResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Project", slug));
    }
}
