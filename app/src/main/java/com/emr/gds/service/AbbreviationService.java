package com.emr.gds.service;

import com.emr.gds.domain.AbbreviationEntry;
import com.emr.gds.repository.AbbreviationRepository;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * Use-case service for managing abbreviations with a shared cache.
 */
public class AbbreviationService {

    private final AbbreviationRepository repository;
    private final Map<String, String> cache;

    public AbbreviationService(AbbreviationRepository repository, Map<String, String> cache) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    public Map<String, String> loadAll() throws SQLException {
        cache.clear();
        cache.putAll(repository.findAll());
        return cache;
    }

    public Map<String, String> getAbbreviations() {
        return cache;
    }

    public void add(AbbreviationEntry entry) throws SQLException {
        repository.insert(entry.shortForm(), entry.fullForm());
        cache.put(entry.shortForm(), entry.fullForm());
    }

    public void update(String originalShortForm, AbbreviationEntry entry) throws SQLException {
        repository.update(originalShortForm, entry.shortForm(), entry.fullForm());
        cache.remove(originalShortForm);
        cache.put(entry.shortForm(), entry.fullForm());
    }

    public boolean delete(String shortForm) throws SQLException {
        boolean deleted = repository.delete(shortForm);
        if (deleted) {
            cache.remove(shortForm);
        }
        return deleted;
    }
}
