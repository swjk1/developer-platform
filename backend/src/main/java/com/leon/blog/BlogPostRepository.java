package com.leon.blog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    /** Drafts must never leak through a public endpoint, so status is part of the lookup. */
    Optional<BlogPost> findBySlugAndStatus(String slug, BlogPostStatus status);

    List<BlogPost> findByStatusOrderByPublishedAtDesc(BlogPostStatus status);
}
