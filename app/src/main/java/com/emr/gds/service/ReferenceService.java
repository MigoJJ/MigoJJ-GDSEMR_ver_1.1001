package com.emr.gds.service;

import com.emr.gds.features.ReferenceFile.ReferenceItem;
import com.emr.gds.repository.ReferenceRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ReferenceService {

    private final ReferenceRepository referenceRepository;

    public ReferenceService(ReferenceRepository referenceRepository) {
        this.referenceRepository = referenceRepository;
    }

    public ObservableList<ReferenceItem> findAllReferences() {
        List<ReferenceItem> items = referenceRepository.findAll();
        return FXCollections.observableArrayList(items);
    }

    public ReferenceItem saveReference(ReferenceItem item) {
        return referenceRepository.save(item);
    }

    public void deleteReference(ReferenceItem item) {
        referenceRepository.delete(item);
    }
}
