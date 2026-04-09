package com.emr.gds.features.medication.model;

import java.util.List;

public record MedicationGroup(String title, List<MedicationItem> medications) {}
