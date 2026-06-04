package com.emr.gds.server.service;

import com.emr.gds.server.model.TemplateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateServiceTest {

    private TemplateService service;

    @BeforeEach
    void setUp() {
        service = new TemplateService();
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void findAll_initialState_returnsSeedTemplates() {
        List<TemplateDto> all = service.findAll();
        assertThat(all).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findAll_containsDemoVisit() {
        assertThat(service.findAll())
                .anyMatch(t -> t.id().equals("demo-visit"));
    }

    // ── findById ─────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsTemplate() {
        Optional<TemplateDto> result = service.findById("demo-visit");
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Demo Visit");
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(service.findById("not-exist")).isEmpty();
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    void create_newTemplate_persistsAndReturns() {
        TemplateDto created = service.create("Thyroid Follow Up", "TSH result reviewed.");

        assertThat(created.name()).isEqualTo("Thyroid Follow Up");
        assertThat(created.body()).isEqualTo("TSH result reviewed.");
        assertThat(created.id()).isNotBlank();
    }

    @Test
    void create_slugifiesNameToId() {
        TemplateDto created = service.create("Hypertension Check", "BP measured.");

        assertThat(created.id()).isEqualTo("hypertension-check");
    }

    @Test
    void create_specialCharsInName_slugified() {
        TemplateDto created = service.create("DM Type 2!!", "FBS check.");

        assertThat(created.id()).doesNotContain("!").doesNotContain(" ");
    }

    @Test
    void create_duplicateName_appendsSuffix() {
        service.create("Follow Up", "body 1");
        TemplateDto second = service.create("Follow Up", "body 2");

        assertThat(second.id()).isEqualTo("follow-up-1");
    }

    @Test
    void create_nullBody_treatedAsEmpty() {
        TemplateDto created = service.create("Empty Template", null);

        assertThat(created.body()).isEmpty();
    }

    @Test
    void create_isRetrievableByFindById() {
        TemplateDto created = service.create("New Template", "content");

        assertThat(service.findById(created.id())).isPresent();
    }

    @Test
    void create_appearsInFindAll() {
        service.create("Unique Template XYZ", "body");

        assertThat(service.findAll())
                .anyMatch(t -> t.name().equals("Unique Template XYZ"));
    }

    // ── slugify 경계값 ────────────────────────────────────────────────────

    @Test
    void create_blankName_generatesNonBlankId() {
        TemplateDto created = service.create("   ", "body");
        assertThat(created.id()).isNotBlank();
    }
}
