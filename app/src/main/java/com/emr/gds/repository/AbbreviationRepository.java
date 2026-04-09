package com.emr.gds.repository;

import java.sql.SQLException;
import java.util.Map;

/**
 * Repository for loading and persisting abbreviations.
 */
public interface AbbreviationRepository {
    Map<String, String> findAll() throws SQLException;

    void insert(String shortForm, String fullForm) throws SQLException;

    void update(String originalShortForm, String newShortForm, String newFullForm) throws SQLException;

    boolean delete(String shortForm) throws SQLException;
}
