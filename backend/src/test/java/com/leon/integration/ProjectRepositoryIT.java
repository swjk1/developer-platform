package com.leon.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.leon.project.Project;
import com.leon.project.ProjectRepository;
import com.leon.project.ProjectStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProjectRepositoryIT extends IntegrationTestBase {

    @Autowired
    ProjectRepository repository;

    @Test
    void migrationSeedsTheThreeFeaturedProjects() {
        List<Project> featured = repository.findByFeaturedOrderByDisplayOrderAscIdAsc(true);

        assertThat(featured)
                .extracting(Project::getSlug)
                .containsExactly("navigation-assistant", "keyguard", "cookpilot");
    }

    @Test
    void technologiesSurviveTheRoundTripInDeclaredOrder() {
        Project nav = repository.findBySlug("navigation-assistant").orElseThrow();

        assertThat(nav.getTechnologies()).startsWith("Kotlin", "ARCore", "YOLO");
    }

    @Test
    void listingIsOrderedByDisplayOrder() {
        List<Project> all = repository.findAllByOrderByDisplayOrderAscIdAsc();

        assertThat(all).isSortedAccordingTo((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()));
    }

    @Test
    void filteringByStatusExcludesNothingWhenEverythingIsActive() {
        assertThat(repository.findByStatusOrderByDisplayOrderAscIdAsc(ProjectStatus.ACTIVE))
                .hasSameSizeAs(repository.findAll());
    }

    @Test
    void unknownSlugResolvesToEmptyRatherThanThrowing() {
        assertThat(repository.findBySlug("does-not-exist")).isEmpty();
    }
}
