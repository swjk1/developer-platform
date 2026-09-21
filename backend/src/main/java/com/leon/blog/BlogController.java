package com.leon.blog;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
public class BlogController {

    private final BlogService service;

    public BlogController(BlogService service) {
        this.service = service;
    }

    @GetMapping
    public List<BlogPostSummaryResponse> list() {
        return service.findPublished();
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BlogPostResponse> bySlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.findPublishedBySlug(slug));
    }
}
