package com.emr.gds.features.bone.domain;

public record DexaDiagnosisResult(
    DexaDiagnosis diagnosis,
    DexaMeasurement referenceMeasurement,
    ScoreType referenceScoreType,
    String rationale
) {}
