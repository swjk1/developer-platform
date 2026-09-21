package com.leon.blog;

import java.time.Instant;

/** Full post, including body. Returned by the single-post endpoint only. */
public record BlogPostResponse(
        String slug,
        String title,
        String summary,
        String content,
        Instant publishedAt) {

    public static BlogPostResponse from(BlogPost p) {
        return new BlogPostResponse(p.getSlug(), p.getTitle(), p.getSummary(), p.getContent(), p.getPublishedAt());
    }
}
