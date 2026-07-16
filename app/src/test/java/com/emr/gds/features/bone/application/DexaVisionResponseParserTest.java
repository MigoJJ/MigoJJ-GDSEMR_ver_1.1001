package com.emr.gds.features.bone.application;

import com.emr.gds.features.bone.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DexaVisionResponseParser JSON 파싱 테스트")
class DexaVisionResponseParserTest {

    @Test
    @DisplayName("정상 JSON 응답 파싱")
    void testParseValidResponse() {
        String json = """
            {
              "patientName": "John Doe",
              "patientId": "P12345",
              "birthDate": "1965-05-15",
              "age": 60,
              "sex": "FEMALE",
              "heightCm": 165.0,
              "weightKg": 70.0,
              "examDate": "2026-07-16",
              "analysisDate": "2026-07-16",
              "spine": [
                {"region": "L1-L4", "bmd": 1.0, "tScore": -1.5, "zScore": null}
              ],
              "femur": [
                {"region": "Neck", "bmd": 0.8, "tScore": -2.8, "zScore": null}
              ]
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getPatientName()).isEqualTo("John Doe");
        assertThat(report.getPatientId()).isEqualTo("P12345");
        assertThat(report.getAge()).isEqualTo(60);
        assertThat(report.getSex()).isEqualTo(Sex.FEMALE);
        assertThat(report.getHeightCm()).isEqualTo(165.0);
        assertThat(report.getWeightKg()).isEqualTo(70.0);
        assertThat(report.getSpineMeasurements()).hasSize(1);
        assertThat(report.getFemurMeasurements()).hasSize(1);
    }

    @Test
    @DisplayName("마크다운 코드펜스 제거")
    void testStripCodeFences() {
        String json = """
            ```json
            {
              "patientName": "Jane Doe",
              "spine": [],
              "femur": [{"region": "Total", "bmd": 1.0, "tScore": 0.5, "zScore": null}]
            }
            ```
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getPatientName()).isEqualTo("Jane Doe");
        assertThat(report.getFemurMeasurements()).hasSize(1);
    }

    @Test
    @DisplayName("코드펜스 없이 JSON만")
    void testParseJsonWithoutFences() {
        String json = """
            {
              "patientName": "Test User",
              "spine": [{"region": "L1", "bmd": 1.0, "tScore": null, "zScore": -1.0}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getPatientName()).isEqualTo("Test User");
        assertThat(report.getSpineMeasurements()).hasSize(1);
        assertThat(report.getSpineMeasurements().get(0).zScore()).isEqualTo(-1.0);
    }

    @Test
    @DisplayName("null 값 처리")
    void testParseNullValues() {
        String json = """
            {
              "patientName": null,
              "patientId": null,
              "birthDate": null,
              "age": null,
              "sex": null,
              "spine": [{"region": "L1-L4", "bmd": 1.0, "tScore": -1.5, "zScore": null}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getPatientName()).isNull();
        assertThat(report.getPatientId()).isNull();
        assertThat(report.getAge()).isNull();
        assertThat(report.getSex()).isNull();
    }

    @Test
    @DisplayName("여러 척추 측정치 파싱")
    void testParseMultipleSpineMeasurements() {
        String json = """
            {
              "patientName": "John",
              "spine": [
                {"region": "L1", "bmd": 1.05, "tScore": -1.2, "zScore": null},
                {"region": "L2", "bmd": 1.00, "tScore": -1.5, "zScore": null},
                {"region": "L1-L4", "bmd": 1.00, "tScore": -1.5, "zScore": null}
              ],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getSpineMeasurements()).hasSize(3);
        assertThat(report.getSpineMeasurements().get(0).region()).isEqualTo("L1");
        assertThat(report.getSpineMeasurements().get(2).region()).isEqualTo("L1-L4");
    }

    @Test
    @DisplayName("여러 대퇴부 측정치 파싱")
    void testParseMultipleFemurMeasurements() {
        String json = """
            {
              "patientName": "Jane",
              "spine": [],
              "femur": [
                {"region": "Neck", "bmd": 0.8, "tScore": -2.8, "zScore": null},
                {"region": "Wards", "bmd": 0.7, "tScore": -3.0, "zScore": null},
                {"region": "Total", "bmd": 1.0, "tScore": -1.1, "zScore": null}
              ]
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getFemurMeasurements()).hasSize(3);
        assertThat(report.getFemurMeasurements().stream().map(DexaMeasurement::region))
                .containsExactly("Neck", "Wards", "Total");
    }

    @Test
    @DisplayName("정수를 문자열로 전달받은 경우")
    void testParseIntegerAsString() {
        String json = """
            {
              "patientName": "Test",
              "age": "65",
              "spine": [{"region": "L1-L4", "bmd": "1.0", "tScore": "-2.5", "zScore": "-1.0"}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getAge()).isEqualTo(65);
        assertThat(report.getSpineMeasurements().get(0).bmd()).isEqualTo(1.0);
        assertThat(report.getSpineMeasurements().get(0).tScore()).isEqualTo(-2.5);
    }

    @Test
    @DisplayName("빈 배열 처리 시 예외 발생")
    void testEmptyMeasurementsThrowsException() {
        String json = """
            {
              "patientName": "Empty",
              "spine": [],
              "femur": []
            }
            """;

        assertThatThrownBy(() -> DexaVisionResponseParser.parse(json))
                .isInstanceOf(DexaVisionParseException.class)
                .hasMessageContaining("측정치");
    }

    @Test
    @DisplayName("부위명 없는 측정치는 건너뜀")
    void testSkipMeasurementWithoutRegion() {
        String json = """
            {
              "patientName": "Test",
              "spine": [
                {"region": "L1-L4", "bmd": 1.0, "tScore": -1.5, "zScore": null},
                {"region": null, "bmd": 1.0, "tScore": -1.2, "zScore": null}
              ],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getSpineMeasurements()).hasSize(1);
    }

    @Test
    @DisplayName("잘못된 JSON 형식")
    void testInvalidJsonThrowsException() {
        String json = "not valid json {";

        assertThatThrownBy(() -> DexaVisionResponseParser.parse(json))
                .isInstanceOf(DexaVisionParseException.class)
                .hasMessageContaining("JSON");
    }

    @Test
    @DisplayName("빈 응답")
    void testEmptyResponseThrowsException() {
        assertThatThrownBy(() -> DexaVisionResponseParser.parse(""))
                .isInstanceOf(DexaVisionParseException.class)
                .hasMessageContaining("비어있");
    }

    @Test
    @DisplayName("Z-score만 있는 경우")
    void testParseZScoreOnly() {
        String json = """
            {
              "patientName": "ZScore Test",
              "age": 16,
              "sex": "FEMALE",
              "spine": [{"region": "L1-L4", "bmd": 0.9, "tScore": null, "zScore": -1.8}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getSpineMeasurements().get(0).tScore()).isNull();
        assertThat(report.getSpineMeasurements().get(0).zScore()).isEqualTo(-1.8);
    }

    @Test
    @DisplayName("날짜 형식 파싱")
    void testParseDateFormat() {
        String json = """
            {
              "patientName": "Date Test",
              "birthDate": "1960-01-15",
              "examDate": "2026-07-16",
              "spine": [{"region": "L1-L4", "bmd": 1.0, "tScore": -1.0, "zScore": null}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);

        assertThat(report.getBirthDate()).isEqualTo(LocalDate.of(1960, 1, 15));
        assertThat(report.getExamDate()).isEqualTo(LocalDate.of(2026, 7, 16));
    }

    @Test
    @DisplayName("성별 파싱 (대소문자 무관)")
    void testParseSexCaseInsensitive() {
        String jsonMale = """
            {
              "patientName": "Male",
              "sex": "male",
              "spine": [{"region": "L1-L4", "bmd": 1.0, "tScore": 0.5, "zScore": null}],
              "femur": []
            }
            """;

        DexaReport report = DexaVisionResponseParser.parse(jsonMale);
        assertThat(report.getSex()).isEqualTo(Sex.MALE);
    }

    @Test
    @DisplayName("코드펜스 추가 텍스트 제거")
    void testStripCodeFencesWithExtraText() {
        String json = """
            Sure, here is the extracted data:
            ```json
            {
              "patientName": "Test",
              "spine": [{"region": "L1-L4", "bmd": 1.0, "tScore": -1.5, "zScore": null}],
              "femur": []
            }
            ```
            Hope this helps!
            """;

        DexaReport report = DexaVisionResponseParser.parse(json);
        assertThat(report.getPatientName()).isEqualTo("Test");
    }
}
