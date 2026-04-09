package com.emr.gds.repository;

import com.emr.gds.features.ReferenceFile.ReferenceItem;
import java.util.List;
import java.util.Optional;

public interface ReferenceRepository {
    ReferenceItem save(ReferenceItem item); // Saves a new item or updates an existing one
    void delete(ReferenceItem item);
    List<ReferenceItem> findAll();
    Optional<ReferenceItem> findById(int id);
    Optional<ReferenceItem> findByCategoryAndContents(String category, String contents);
    boolean existsByCategoryAndContents(String category, String contents, int excludeId);
    List<String> findDistinctCategories();
    List<ReferenceItem> search(String query, String category);
    // Add other methods as needed, e.g., findByCategory, findByContents
}
