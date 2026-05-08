package com.emr.gds.features.thyroid;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ThyroidRiskCalculatorTest {

    @Test
    public void testCalculateFullReplacementDose() {
        assertEquals(112.0, ThyroidRiskCalculator.calculateFullReplacementDose(70.0));
        assertEquals(80.0, ThyroidRiskCalculator.calculateFullReplacementDose(50.0));
        assertEquals(160.0, ThyroidRiskCalculator.calculateFullReplacementDose(100.0));
    }

    @Test
    public void testCalculateAtaRisk() {
        // High Risk
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(true, false, false, false, false, 0, 0.0));
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(false, true, false, false, false, 0, 0.0));
        assertEquals("High Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, true, false, false, 0, 0.0));

        // Intermediate Risk
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, true, false, 0, 0.0));
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, true, 0, 0.0));
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 6, 0.0));
        assertEquals("Intermediate Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 0, 3.5));

        // Low Risk
        assertEquals("Low Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 0, 0.0));
        assertEquals("Low Risk", ThyroidRiskCalculator.calculateAtaRisk(false, false, false, false, false, 5, 3.0));
    }

    @Test
    public void testCalculateTiRads() {
        // TR1 Benign (0 points)
        ThyroidRiskCalculator.TiRadsResult tr1 = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_CYSTIC_SPONGI,
                ThyroidRiskCalculator.TiRadsFeature.ECHO_ANECHOIC,
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE
        );
        assertEquals(0, tr1.score);
        assertTrue(tr1.level.startsWith("TR1"));

        // TR5 Highly Suspicious (High points)
        ThyroidRiskCalculator.TiRadsResult tr5 = ThyroidRiskCalculator.calculateTiRads(
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID,     // 2
                ThyroidRiskCalculator.TiRadsFeature.ECHO_VERY_HYPO, // 3
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_TALLER,   // 3
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_EXTRA,   // 3
                ThyroidRiskCalculator.TiRadsFeature.FOCI_PUNCTATE   // 3
        );
        assertEquals(14, tr5.score);
        assertTrue(tr5.level.startsWith("TR5"));
    }
}
