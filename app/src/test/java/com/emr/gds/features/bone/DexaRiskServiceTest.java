package com.emr.gds.features.bone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DexaRiskServiceTest {

    private DexaRiskService service;

    @BeforeEach
    void setUp() {
        service = new DexaRiskService();
    }

    // ── diagnose — T-Score 기준 (WHO) ─────────────────────────────────────

    @Test
    void diagnose_tScore_above_minus1_isNormal() {
        assertThat(service.diagnose(0.0, true, false)).isEqualTo("Normal Bone Density");
        assertThat(service.diagnose(-0.9, true, false)).isEqualTo("Normal Bone Density");
        assertThat(service.diagnose(-1.0, true, false)).isEqualTo("Normal Bone Density");
    }

    @Test
    void diagnose_tScore_minus1_to_minus2_5_isOsteopenia() {
        assertThat(service.diagnose(-1.1, true, false)).isEqualTo("Osteopenia");
        assertThat(service.diagnose(-2.0, true, false)).isEqualTo("Osteopenia");
        assertThat(service.diagnose(-2.4, true, false)).isEqualTo("Osteopenia");
    }

    @Test
    void diagnose_tScore_minus2_5_noFracture_isOsteoporosis() {
        assertThat(service.diagnose(-2.5, true, false)).isEqualTo("Osteoporosis");
        assertThat(service.diagnose(-3.0, true, false)).isEqualTo("Osteoporosis");
    }

    @Test
    void diagnose_tScore_minus2_5_withFracture_isSevereOsteoporosis() {
        assertThat(service.diagnose(-2.5, true, true)).isEqualTo("Severe Osteoporosis");
        assertThat(service.diagnose(-3.5, true, true)).isEqualTo("Severe Osteoporosis");
    }

    // ── diagnose — T-Score 경계값 ─────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "-1.0,  Normal Bone Density",
            "-1.01, Osteopenia",
            "-2.49, Osteopenia",
            "-2.5,  Osteoporosis",
            "-2.51, Osteoporosis"
    })
    void diagnose_tScore_boundaries(double score, String expected) {
        assertThat(service.diagnose(score, true, false)).isEqualTo(expected);
    }

    // ── diagnose — Z-Score 기준 ───────────────────────────────────────────

    @Test
    void diagnose_zScore_above_minus2_isWithinRange() {
        assertThat(service.diagnose(-1.9, false, false)).isEqualTo("Within expected range for age");
        assertThat(service.diagnose(0.0,  false, false)).isEqualTo("Within expected range for age");
    }

    @Test
    void diagnose_zScore_minus2_orBelow_isBelowRange() {
        assertThat(service.diagnose(-2.0, false, false)).isEqualTo("Below expected range for age");
        assertThat(service.diagnose(-3.0, false, false)).isEqualTo("Below expected range for age");
    }

    // ── generateReport — 헤더 및 내용 ────────────────────────────────────

    @Test
    void generateReport_containsDexaHeader() {
        String report = service.generateReport(-2.5, true, 65, "Female",
                false, true, false, false, false);
        assertThat(report).contains("DEXA Report");
    }

    @Test
    void generateReport_containsTodayDate() {
        String today = LocalDate.now().toString();
        String report = service.generateReport(-2.5, true, 65, "Female",
                false, true, false, false, false);
        assertThat(report).contains(today);
    }

    @Test
    void generateReport_containsDiagnosis() {
        String report = service.generateReport(-2.5, true, 65, "Female",
                false, false, false, false, false);
        assertThat(report).contains("Osteoporosis");
    }

    @Test
    void generateReport_female_containsMenopausalFactor() {
        String report = service.generateReport(-2.5, true, 65, "Female",
                false, true, false, false, false);
        assertThat(report).contains("Menopausal");
    }

    @Test
    void generateReport_male_doesNotContainMenopausalFactor() {
        String report = service.generateReport(-2.5, true, 65, "Male",
                false, false, false, false, false);
        assertThat(report).doesNotContain("Menopausal");
    }

    // ── generateReport — 치료 권고 ────────────────────────────────────────

    @Test
    void generateReport_tScore_minus2_5_recommendsBisphosphonate() {
        String report = service.generateReport(-2.5, true, 70, "Female",
                false, false, false, false, false);
        assertThat(report).contains("bisphosphonate");
    }

    @Test
    void generateReport_osteopenia_recommendsLifestyleModification() {
        String report = service.generateReport(-1.5, true, 55, "Female",
                false, false, false, false, false);
        assertThat(report).contains("Lifestyle modification");
    }

    @Test
    void generateReport_normalBoneDensity_noTreatmentRecommendation() {
        String report = service.generateReport(-0.5, true, 45, "Female",
                false, false, false, false, false);
        assertThat(report)
                .doesNotContain("bisphosphonate")
                .doesNotContain("Lifestyle modification");
    }

    @Test
    void generateReport_severeFracture_recommendsBisphosphonate() {
        String report = service.generateReport(-3.0, true, 78, "Female",
                true, false, false, false, false);
        assertThat(report).contains("Severe Osteoporosis");
        assertThat(report).contains("bisphosphonate");
    }
}
