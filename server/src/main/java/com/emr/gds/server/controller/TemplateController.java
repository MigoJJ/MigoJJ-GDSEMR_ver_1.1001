package com.emr.gds.server.controller;

import com.emr.gds.server.model.TemplateDto;
import com.emr.gds.server.service.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {
    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<TemplateDto> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> get(@PathVariable String id) {
        return ResponseEntity.of(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TemplateDto> create(@RequestBody TemplateRequest request) {
        TemplateDto created = service.create(request.name(), request.body());
        return ResponseEntity.created(URI.create("/api/v1/templates/" + created.id())).body(created);
    }

    public record TemplateRequest(String name, String body) {
    }
}
