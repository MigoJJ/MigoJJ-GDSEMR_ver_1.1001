package com.emr.gds.features.bone.domain;

import java.util.List;

public interface DexaRepository {
    void save(DexaReport report);
    List<DexaReport> findRecent(int limit);
    DexaReport findById(long id);
}
