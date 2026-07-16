package com.emr.gds.features.bone.application;

import com.emr.gds.features.bone.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DexaDiagnosisService 2026 한국 기준 진단 로직 테스트")
class DexaDiagnosisServiceTest {

    private DexaDiagnosisService service;

    @BeforeEach
    void setUp() {
        service = new DexaDiagnosisService();
    }

    // ============ T-SCORE 경계값 테스트 ============

    @Test
    @DisplayName("T-score >= -1.0: 정상 진단")
    void testTScoreNormal() {
        DexaReport report = createReportWithTScore(-0.8);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.NORMAL);
        assertThat(result.referenceScoreType()).isEqualTo(ScoreType.T_SCORE);
    }

    @Test
    @DisplayName("T-score = -1.0 (경계): 정상 진단")
    void testTScoreBoundaryNegativeOne() {
        DexaReport report = createReportWithTScore(-1.0);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.NORMAL);
    }

    @Test
    @DisplayName("T-score = -1.1 (경계): 골감소증 진단")
    void testTScoreBoundaryOsteopenia() {
        DexaReport report = createReportWithTScore(-1.1);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPENIA);
    }

    @Test
    @DisplayName("T-score = -2.0 (중간): 골감소증 진단")
    void testTScoreOsteopeniaMid() {
        DexaReport report = createReportWithTScore(-2.0);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPENIA);
    }

    @Test
    @DisplayName("T-score = -2.5 (경계): 골다공증 진단")
    void testTScoreBoundaryOsteoporosis() {
        DexaReport report = createReportWithTScore(-2.5);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS);
    }

    @Test
    @DisplayName("T-score = -2.6: 골다공증 진단")
    void testTScoreOsteoporosis() {
        DexaReport report = createReportWithTScore(-2.6);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS);
    }

    @Test
    @DisplayName("T-score = -3.5 (심각): 골다공증 진단")
    void testTScoreSevere() {
        DexaReport report = createReportWithTScore(-3.5);
        DexaRiskFactors factors = createDefaultRiskFactors();

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS);
    }

    // ============ 취약골절(FRAGILITY FRACTURE) 테스트 ============

    @Test
    @DisplayName("T-score <= -2.5 + 취약골절 = 심한 골다공증")
    void testFragilityFractureWithOsteoporosis() {
        DexaReport report = createReportWithTScore(-2.8);
        DexaRiskFactors factors = new DexaRiskFactors(true, false, true, false, false, false);

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.SEVERE_OSTEOPOROSIS);
    }

    @Test
    @DisplayName("T-score > -2.5 + 취약골절 = 골다공증 (취약골절 플래그만으로 상향 안 함)")
    void testFragilityFractureWithoutOsteoporosis() {
        DexaReport report = createReportWithTScore(-2.0);
        DexaRiskFactors factors = new DexaRiskFactors(true, false, true, false, false, false);

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPENIA);
    }

    // ============ 고관절/척추 취약골절 오버라이드 테스트 ============

    @Test
    @DisplayName("고관절/척추 취약골절: T-score 무관하게 골다공증 진단 (최우선 오버라이드)")
    void testHipVertebralFractureOverride() {
        DexaReport report = createReportWithTScore(0.5); // 정상 T-score
        DexaRiskFactors factors = new DexaRiskFactors(false, true, false, false, false, false);

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS_BY_FRACTURE);
    }

    @Test
    @DisplayName("고관절/척추 취약골절: fragilityFracture 플래그 상태와 무관")
    void testHipVertebralFractureIgnoresFragilityFlag() {
        DexaReport report = createReportWithTScore(1.0);
        DexaRiskFactors factors = new DexaRiskFactors(true, true, false, false, false, false);

        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS_BY_FRACTURE);
    }

    // ============ 여러 측정치 중 최저값 선택 테스트 ============

    @Test
    @DisplayName("여러 측정치: 최저 T-score 선택")
    void testSelectLowestTScore() {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        // L1-L4: -1.5 (OSTEOPENIA)
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, -1.5, null));
        // Femoral Neck: -2.8 (OSTEOPOROSIS)
        report.getFemurMeasurements().add(new DexaMeasurement("Neck", SkeletalSite.FEMUR, 0.8, -2.8, null));
        // Total Hip: -1.2 (OSTEOPENIA)
        report.getFemurMeasurements().add(new DexaMeasurement("Total Hip", SkeletalSite.FEMUR, 1.0, -1.2, null));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, true, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS);
        assertThat(result.referenceMeasurement().tScore()).isEqualTo(-2.8);
    }

    // ============ Z-SCORE 테스트 ============

    @Test
    @DisplayName("미성년자(age<18): Z-score 사용")
    void testZScoreForChildren() {
        DexaReport report = new DexaReport();
        report.setAge(15);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -1.5));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, false, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.referenceScoreType()).isEqualTo(ScoreType.Z_SCORE);
    }

    @Test
    @DisplayName("폐경전 여성: Z-score 사용")
    void testZScoreForPremenopausalWomen() {
        DexaReport report = new DexaReport();
        report.setAge(40);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -1.8));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, false, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.referenceScoreType()).isEqualTo(ScoreType.Z_SCORE);
    }

    @Test
    @DisplayName("50세 미만 남성: Z-score 사용")
    void testZScoreForMenUnder50() {
        DexaReport report = new DexaReport();
        report.setAge(45);
        report.setSex(Sex.MALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -1.8));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, false, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.referenceScoreType()).isEqualTo(ScoreType.Z_SCORE);
    }

    @Test
    @DisplayName("Z-score <= -2.0: 연령대비 저하")
    void testZScoreBelowExpected() {
        DexaReport report = new DexaReport();
        report.setAge(16);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -2.0));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, false, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.BELOW_EXPECTED_RANGE_FOR_AGE);
    }

    @Test
    @DisplayName("Z-score > -2.0: 연령대비 정상범위")
    void testZScoreWithinExpected() {
        DexaReport report = new DexaReport();
        report.setAge(16);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -1.5));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, false, false, false, false);
        DexaDiagnosisResult result = service.diagnose(report, factors);

        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.WITHIN_EXPECTED_RANGE_FOR_AGE);
    }

    // ============ 기준 부위 필터링 테스트 ============

    @Test
    @DisplayName("기준 부위만 선택 (L1, L2 개별 값은 제외)")
    void testFilterNonReferenceRegions() {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        // L1, L2 개별 값 (제외)
        report.getSpineMeasurements().add(new DexaMeasurement("L1", SkeletalSite.SPINE, 1.0, -1.0, null));
        report.getSpineMeasurements().add(new DexaMeasurement("L2", SkeletalSite.SPINE, 0.95, -1.2, null));
        // L1-L4 (포함)
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 0.99, -1.5, null));

        // postmenopausal=true to use T-score path
        DexaRiskFactors factors = createDefaultRiskFactors();
        DexaDiagnosisResult result = service.diagnose(report, factors);

        // 최저값은 -1.5 (L1-L4)
        assertThat(result.referenceMeasurement().region()).isEqualTo("L1-L4");
    }

    @Test
    @DisplayName("Ward, Troch 같은 비기준 대퇴부 부위는 제외")
    void testFilterNonReferenceFemurRegions() {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, -0.8, null));
        // Ward, Troch (제외)
        report.getFemurMeasurements().add(new DexaMeasurement("Wards", SkeletalSite.FEMUR, 0.8, -2.8, null));
        report.getFemurMeasurements().add(new DexaMeasurement("Troch", SkeletalSite.FEMUR, 0.7, -2.5, null));
        // Total Hip (포함)
        report.getFemurMeasurements().add(new DexaMeasurement("Total Hip", SkeletalSite.FEMUR, 1.0, -0.5, null));

        DexaRiskFactors factors = createDefaultRiskFactors();
        DexaDiagnosisResult result = service.diagnose(report, factors);

        // 최저값은 -0.8 (L1-L4), Ward/Troch는 무시됨
        assertThat(result.diagnosis()).isEqualTo(DexaDiagnosis.NORMAL);
    }

    // ============ 예외 케이스 ============

    @Test
    @DisplayName("기준 부위가 없으면 IllegalStateException 발생")
    void testNoReferenceMeasurements() {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        // L1, L2 개별 값만 (기준 부위 아님)
        report.getSpineMeasurements().add(new DexaMeasurement("L1", SkeletalSite.SPINE, 1.0, -1.5, null));
        report.getSpineMeasurements().add(new DexaMeasurement("L2", SkeletalSite.SPINE, 0.95, -1.2, null));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, true, false, false, false);

        assertThatThrownBy(() -> service.diagnose(report, factors))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("기준 부위");
    }

    @Test
    @DisplayName("T-score만 있는 경우 T-score가 없으면 예외")
    void testNoTScoreValues() {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, null));

        DexaRiskFactors factors = new DexaRiskFactors(false, false, true, false, false, false);

        assertThatThrownBy(() -> service.diagnose(report, factors))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("값이 없");
    }

    // ============ Helper Methods ============

    private DexaReport createReportWithTScore(double tScore) {
        DexaReport report = new DexaReport();
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, tScore, null));
        return report;
    }

    private DexaRiskFactors createDefaultRiskFactors() {
        return new DexaRiskFactors(false, false, true, false, false, false);
    }
}
