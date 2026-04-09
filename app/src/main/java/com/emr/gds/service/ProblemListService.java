package com.emr.gds.service;

import com.emr.gds.repository.ProblemRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Use-case service for the persistent problem list.
 */
public class ProblemListService {

    private final ProblemRepository repository;

    public ProblemListService(ProblemRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void initialize() throws SQLException {
        repository.init();
    }

    public List<String> loadAll() throws SQLException {
        return repository.findAll();
    }

    public void add(String problemText) throws SQLException {
        repository.insert(problemText);
    }

    public boolean delete(String problemText) throws SQLException {
        return repository.delete(problemText);
    }
}
