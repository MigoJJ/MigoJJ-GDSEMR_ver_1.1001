package com.emr.gds.features.bone.domain;

public record DexaRiskFactors(
    boolean fragilityFracture,
    boolean hipOrVertebralFracture,
    boolean postmenopausal,
    boolean onHrt,
    boolean priorTah,
    boolean kidneyStones
) {}
