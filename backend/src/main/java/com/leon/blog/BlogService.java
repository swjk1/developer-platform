package com.leon.blog;

import com.leon.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BlogService {

    private final BlogPostRepository repository;

    public BlogService(BlogPostRepository repository) {
        this.repository = repository;
    }

    public List<BlogPostSummaryResponse> findPublished() {
        return repository.findByStatusOrderByPublishedAtDesc(BlogPostStatus.PUBLISHED)
                .stream()
                .map(BlogPostSummaryResponse::from)
                .toList();
    }

    /**
     * A draft is reported as 404 rather than 403. Saying "this exists but you
     * may not see it" leaks the existence of unpublished work.
     */
    public BlogPostResponse findPublishedBySlug(String slug) {
        return repository.findBySlugAndStatus(slug, BlogPostStatus.PUBLISHED)
                .map(BlogPostResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Post", slug));
    }
}
