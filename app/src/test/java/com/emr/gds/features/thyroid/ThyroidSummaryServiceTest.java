package com.emr.gds.features.thyroid;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThyroidSummaryServiceTest {

    private final ThyroidSummaryService service = new ThyroidSummaryService();

    @Test
    void includesStoredTiRadsResultInSummary() {
        ThyroidEntry entry = new ThyroidEntry();
        entry.setTiRadsLevel("TR5 (Highly Suspicious)");
        entry.setTiRadsScore(8);

        String summary = service.buildSpecialistSummary(entry, Collections.emptyMap(), "");

        assertTrue(summary.contains("Nodule/TI-RADS: TR5 (Highly Suspicious), Score: 8"));
    }

    @Test
    void printsGoiterSizeWhenInputAlreadyContainsCc() {
        ThyroidEntry entry = new ThyroidEntry();
        entry.setGoiterSize("20 cc");

        String summary = service.buildSpecialistSummary(entry, Collections.emptyMap(), "");

        assertTrue(summary.contains("Physical Exam: Goiter size 20 cc"));
    }

    @Test
    void appendsCcToNumericGoiterSize() {
        ThyroidEntry entry = new ThyroidEntry();
        entry.setGoiterSize("20");

        String summary = service.buildSpecialistSummary(entry, Collections.emptyMap(), "");

        assertTrue(summary.contains("Physical Exam: Goiter size 20 cc"));
    }

    @Test
    void usesConsistentLabReferenceUnitsAndIndicators() {
        ThyroidEntry entry = new ThyroidEntry();
        entry.setFreeT4(2.0);
        entry.setTotalT3(70.0);
        entry.setReverseT3(30.0);

        String summary = service.buildSpecialistSummary(entry, Collections.emptyMap(), "");

        assertTrue(summary.contains("(0.8-1.8 ng/dL)"));
        assertTrue(summary.contains("(80-200 ng/dL)"));
        assertTrue(summary.contains("(8-25 ng/dL)"));
        assertTrue(summary.contains("fT4"));
        assertTrue(summary.contains("▲"));
        assertTrue(summary.contains("▽"));
        assertFalse(summary.contains("10.6-19.4 ng/L"));
        assertFalse(summary.contains("90-350 pg/mL"));
    }

    @Test
    void includesEnteredPhysicalExamTreatmentImagingAndTrackingResults() {
        ThyroidEntry entry = new ThyroidEntry();
        entry.setPhysicalExamNote("Physical Exam:\n- Goiter Ruled: Goiter ruled out\n- Tenderness: Non-tender");
        entry.setLt4DoseMcgPerDay(75.0);
        entry.setAtdName("MMI");
        entry.setAtdDoseMgPerDay(5.0);
        entry.setBetaBlockerName("Propranolol");
        entry.setBetaBlockerDose("10 mg bid");
        entry.setUsDate(LocalDate.of(2026, 5, 13));
        entry.setUsSummary("No suspicious lymph nodes.");
        entry.setScanSummary("Low uptake.");
        entry.setRaiDone(true);
        entry.setRaiDoseMci(30.0);
        entry.setRaiDate(LocalDate.of(2026, 4, 1));
        entry.setClinicianNote("Discussed warning symptoms.");

        String summary = service.buildSpecialistSummary(entry, Collections.emptyMap(), "");

        assertTrue(summary.contains("     |\tPhysical Exam:\n     |\t\t- Goiter Ruled: Goiter ruled out\n     |\t\t- Tenderness: Non-tender"));
        assertTrue(summary.contains("Treatment: LT4 75 mcg/day; MMI 5 mg/day; Propranolol 10 mg bid"));
        assertTrue(summary.contains("Ultrasound (2026-05-13): No suspicious lymph nodes."));
        assertTrue(summary.contains("Scan: Low uptake."));
        assertTrue(summary.contains("RAI: done; 30 mCi; 2026-04-01"));
        assertTrue(summary.contains("Clinician Note: Discussed warning symptoms."));
    }
}
