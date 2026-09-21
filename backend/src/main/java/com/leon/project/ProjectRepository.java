package com.leon.project;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findBySlug(String slug);

    List<Project> findAllByOrderByDisplayOrderAscIdAsc();

    List<Project> findByStatusOrderByDisplayOrderAscIdAsc(ProjectStatus status);

    List<Project> findByFeaturedOrderByDisplayOrderAscIdAsc(boolean featured);

    List<Project> findByStatusAndFeaturedOrderByDisplayOrderAscIdAsc(ProjectStatus status, boolean featured);
}
