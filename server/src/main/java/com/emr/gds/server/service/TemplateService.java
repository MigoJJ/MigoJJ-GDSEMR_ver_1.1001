package com.emr.gds.server.service;

import com.emr.gds.server.model.TemplateDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory stub for managing EMR templates.
 * Replace the storage layer with a database or repository when ready.
 */
@Service
public class TemplateService {
    private final Map<String, TemplateDto> templates = new LinkedHashMap<>();

    public TemplateService() {
        // Seed a couple of templates so the API has data right away.
        save(new TemplateDto("demo-visit", "Demo Visit",
                "CC> Cough x3d\nPI> 38C fever, no dyspnea\nA> Viral URI\nP> Rest, PO hydration, f/u PRN"));
        save(new TemplateDto("abdominal-pain", "Abdominal Pain",
                "CC> Abdominal pain\nROS> -N/V/D, +bloating\nA> R/O IBS vs dyspepsia\nP> H. pylori test, bland diet"));
    }

    public List<TemplateDto> findAll() {
        return List.copyOf(templates.values());
    }

    public Optional<TemplateDto> findById(String id) {
        return Optional.ofNullable(templates.get(id));
    }

    public TemplateDto create(String name, String body) {
        String id = slugify(name);
        TemplateDto dto = new TemplateDto(id, name, body == null ? "" : body);
        return save(dto);
    }

    private TemplateDto save(TemplateDto dto) {
        templates.put(dto.id(), dto);
        return dto;
    }

    private String slugify(String name) {
        String base = (name == null || name.isBlank())
                ? UUID.randomUUID().toString()
                : name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        String candidate = base.isBlank() ? UUID.randomUUID().toString() : base;
        int suffix = 1;
        while (templates.containsKey(candidate)) {
            candidate = "%s-%d".formatted(base, suffix++);
        }
        return candidate;
    }
}
