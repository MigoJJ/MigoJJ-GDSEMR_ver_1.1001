package com.emr.gds.server.model;

/**
 * Simple DTO representing a reusable EMR template.
 */
public record TemplateDto(String id, String name, String body) {
}
