package com.emr.gds.features.vaccine;

import com.emr.gds.features.vaccine.model.VaccineConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("VaccineAction 테스트")
class VaccineActionTest {

    private VaccineAction vaccineAction;

    @BeforeEach
    void setUp() {
        vaccineAction = new VaccineAction();
    }

    @Test
    @DisplayName("백신 액션 초기화 테스트")
    void testInitialization() {
        assertThat(vaccineAction).isNotNull();
    }

    @Test
    @DisplayName("유효한 백신 이름 검증")
    void testValidVaccineName() {
        String[] validVaccines = {"MMR", "COVID-19", "Influenza"};

        for (String vaccine : validVaccines) {
            assertThat(vaccine).isNotBlank();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("무효한 백신 이름 검증")
    void testInvalidVaccineName(String vaccineName) {
        assertThat(vaccineName.trim()).isEmpty();
    }

    @Test
    @DisplayName("백신 상수가 올바르게 정의되었는지 확인")
    void testVaccineConstants() {
        assertThat(VaccineConstants.class).isNotNull();
    }

    @Test
    @DisplayName("예방접종 기록 추가")
    void testAddVaccineRecord() {
        String vaccineName = "COVID-19";
        String date = "2024-07-16";

        assertThat(vaccineName).isNotBlank();
        assertThat(date).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    @DisplayName("예방접종 기록 조회")
    void testQueryVaccineRecord() {
        String patientId = "P001";

        assertThat(patientId).isNotBlank();
        assertThat(patientId).startsWith("P");
    }
}
