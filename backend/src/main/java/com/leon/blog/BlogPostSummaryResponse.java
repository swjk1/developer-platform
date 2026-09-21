package com.leon.blog;

import java.time.Instant;

/**
 * The listing shape. Deliberately excludes content: the index page renders
 * excerpts, and shipping every post body to render a list of titles is the
 * easiest performance mistake to make here.
 */
public record BlogPostSummaryResponse(
        String slug,
        String title,
        String summary,
        Instant publishedAt) {

    public static BlogPostSummaryResponse from(BlogPost p) {
        return new BlogPostSummaryResponse(p.getSlug(), p.getTitle(), p.getSummary(), p.getPublishedAt());
    }
}
