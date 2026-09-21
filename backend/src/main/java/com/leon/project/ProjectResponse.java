package com.leon.project;

import java.util.List;

/**
 * The public shape of a project (spec section 6). Deliberately not the entity:
 * the API contract is snapshotted and diffed in CI, so it must not move just
 * because a column was renamed.
 */
public record ProjectResponse(
        String slug,
        String name,
        String description,
        String longDescription,
        ProjectStatus status,
        boolean featured,
        String githubUrl,
        String demoUrl,
        List<String> technologies) {

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
                p.getSlug(),
                p.getName(),
                p.getShortDescription(),
                p.getLongDescription(),
                p.getStatus(),
                p.isFeatured(),
                p.getGithubUrl(),
                p.getDemoUrl(),
                List.copyOf(p.getTechnologies()));
    }
}
