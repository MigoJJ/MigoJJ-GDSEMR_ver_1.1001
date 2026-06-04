package com.emr.gds.features.thyroid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ThyroidRiskCalculatorTest {

    // ── LT4 용량 계산 ─────────────────────────────────────────────────────

    @Test
    void levothyroxineDose_70kg_returns112() {
        assertEquals(112.0, ThyroidRiskCalculator.calculateFullReplacementDose(70.0));
    }

    @Test
    void levothyroxineDose_50kg_returns80() {
        assertEquals(80.0, ThyroidRiskCalculator.calculateFullReplacementDose(50.0));
    }

    @Test
    void levothyroxineDose_100kg_returns160() {
        assertEquals(160.0, ThyroidRiskCalculator.calculateFullReplacementDose(100.0));
    }

    // ── ATA 위험도 분류 ───────────────────────────────────────────────────

    @Test
    void ataRisk_grossExtension_isHighRisk() {
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(true, false, false, false, false, 0, 0.0));
    }

    @Test
    void ataRisk_incompleteResection_isHighRisk() {
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(false, true, false, false, false, 0, 0.0));
    }

    @Test
    void ataRisk_distantMetastases_isHighRisk() {
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, true, false, false, 0, 0.0));
    }

    @Test
    void ataRisk_aggressiveHistology_isIntermediateRisk() {
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, true, false, 0, 0.0));
    }

    @Test
    void ataRisk_vascularInvasion_isIntermediateRisk() {
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, true, 0, 0.0));
    }

    // 경계값: 림프절 5개 = Low, 6개 = Intermediate
    @Test
    void ataRisk_exactly5LymphNodes_isLowRisk() {
        assertEquals("Low Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 5, 0.0));
    }

    @Test
    void ataRisk_6LymphNodes_isIntermediateRisk() {
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 6, 0.0));
    }

    // 경계값: 노드 크기 3.0cm = Low, 3.1cm = Intermediate
    @Test
    void ataRisk_nodeSizeExactly3cm_isLowRisk() {
        assertEquals("Low Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 0, 3.0));
    }

    @Test
    void ataRisk_nodeSizeAbove3cm_isIntermediateRisk() {
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 0, 3.1));
    }

    @Test
    void ataRisk_noAdverseFeatures_isLowRisk() {
        assertEquals("Low Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 0, 0.0));
    }

    // ── TI-RADS 계산 ─────────────────────────────────────────────────────

    @Test
    void tiRads_score0_isTR1() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_CYSTIC_SPONGI, // 0
                ThyroidRiskCalculator.TiRadsFeature.ECHO_ANECHOIC,       // 0
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,         // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,       // 0
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE            // 0
        );
        assertThat(result.score).isEqualTo(0);
        assertThat(result.level).startsWith("TR1");
        assertThat(result.recommendation).contains("No FNA");
    }

    @Test
    void tiRads_score2_isTR2() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,    // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_ANECHOIC,  // 0
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,    // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,  // 0
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE       // 0
        );
        assertThat(result.score).isEqualTo(2);
        assertThat(result.level).startsWith("TR2");
    }

    @Test
    void tiRads_score3_isTR3() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,     // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPO,      // 2
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,    // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,  // 0
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE       // 0 → total = 4... wait
        );
        // 경계값 테스트: score == 3 이 되는 조합
        var result3 = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_MIXED,     // 1
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPER_ISO, // 1
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,    // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,  // 0
                ThyroidRiskCalculator.TiRadsFeature.FOCI_MACRO      // 1 → total = 3
        );
        assertThat(result3.score).isEqualTo(3);
        assertThat(result3.level).startsWith("TR3");
        assertThat(result3.recommendation).contains("2.5");
    }

    // 경계값: 점수 4 = TR4 하한, 6 = TR4 상한
    @Test
    void tiRads_score4_isTR4() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,     // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPO,      // 2
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,    // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,  // 0
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE       // 0 → total = 4
        );
        assertThat(result.score).isEqualTo(4);
        assertThat(result.level).startsWith("TR4");
        assertThat(result.recommendation).contains("1.5");
    }

    @Test
    void tiRads_score6_isTR4_upperBoundary() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,       // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPO,        // 2
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,      // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_LOBULATED, // 2
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE         // 0 → total = 6
        );
        assertThat(result.score).isEqualTo(6);
        assertThat(result.level).startsWith("TR4");
    }

    @Test
    void tiRads_score7_isTR5() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,       // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_VERY_HYPO,   // 3
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,      // 0
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_LOBULATED, // 2
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE         // 0 → total = 7
        );
        assertThat(result.score).isEqualTo(7);
        assertThat(result.level).startsWith("TR5");
        assertThat(result.recommendation).contains("1.0");
    }

    @Test
    void tiRads_score14_maxScore_isTR5() {
        var result = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,     // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_VERY_HYPO, // 3
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_TALLER,   // 3
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_EXTRA,   // 3
                ThyroidRiskCalculator.TiRadsFeature.FOCI_PUNCTATE   // 3 → total = 14
        );
        assertThat(result.score).isEqualTo(14);
        assertThat(result.level).startsWith("TR5");
    }

    // ── TiRadsFeature enum 점수 검증 ─────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "COMP_CYSTIC_SPONGI, 0",
            "COMP_MIXED, 1",
            "COMP_SOLID, 2",
            "ECHO_ANECHOIC, 0",
            "ECHO_HYPER_ISO, 1",
            "ECHO_HYPO, 2",
            "ECHO_VERY_HYPO, 3",
            "SHAPE_WIDER, 0",
            "SHAPE_TALLER, 3",
            "MARGIN_SMOOTH, 0",
            "MARGIN_LOBULATED, 2",
            "MARGIN_EXTRA, 3",
            "FOCI_NONE, 0",
            "FOCI_MACRO, 1",
            "FOCI_RIM, 2",
            "FOCI_PUNCTATE, 3"
    })
    void tiRadsFeature_pointsMatchAcrGuideline(String featureName, int expectedPoints) {
        var feature = ThyroidRiskCalculator.TiRadsFeature.valueOf(featureName);
        assertThat(feature.points).isEqualTo(expectedPoints);
    }
}
