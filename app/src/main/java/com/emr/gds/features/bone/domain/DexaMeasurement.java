package com.emr.gds.features.bone.domain;

public record DexaMeasurement(
    String region,
    SkeletalSite site,
    Double bmd,
    Double tScore,
    Double zScore
) {}
