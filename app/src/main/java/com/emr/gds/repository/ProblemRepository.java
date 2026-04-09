package com.emr.gds.repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Repository for accessing the persistent problem list.
 */
public interface ProblemRepository {
    void init() throws SQLException;

    List<String> findAll() throws SQLException;

    void insert(String problemText) throws SQLException;

    boolean delete(String problemText) throws SQLException;
}
