package com.leon.project;

import com.leon.github.GitHubSummary;
import java.util.List;

/**
 * The public shape of a project (spec section 6). Deliberately not the entity:
 * the API contract is snapshotted and diffed in CI, so it must not move just
 * because a column was renamed.
 *
 * @param github the mirrored repository metadata, or null when the project has
 *               no repository or has never been synced. Null rather than a
 *               zero-filled object: "we do not know" and "zero stars" are
 *               different claims and the frontend renders them differently.
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
        List<String> technologies,
        GitHubSummary github) {

    public static ProjectResponse from(Project p, GitHubSummary github) {
        return new ProjectResponse(
                p.getSlug(),
                p.getName(),
                p.getShortDescription(),
                p.getLongDescription(),
                p.getStatus(),
                p.isFeatured(),
                p.getGithubUrl(),
                p.getDemoUrl(),
                List.copyOf(p.getTechnologies()),
                github);
    }
}
