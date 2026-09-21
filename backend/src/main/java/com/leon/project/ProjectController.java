package com.leon.project;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectResponse> list(@RequestParam(required = false) ProjectStatus status,
                                      @RequestParam(required = false) Boolean featured) {
        return service.findAll(status, featured);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProjectResponse> bySlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.findBySlug(slug));
    }
}
