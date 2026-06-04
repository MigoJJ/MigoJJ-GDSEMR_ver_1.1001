package com.emr.gds.features.vaccine.service;

import com.emr.gds.infrastructure.service.EmrBridgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaccineServiceTest {

    @Mock
    private EmrBridgeService bridge;

    private VaccineService service;

    @BeforeEach
    void setUp() {
        service = new VaccineService(bridge);
    }

    // ── logVaccine 성공 경로 ──────────────────────────────────────────────

    @Test
    void logVaccine_allBridgeCallsSucceed_returnsTrue() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        assertThat(service.logVaccine("Shingrix #1/2")).isTrue();
    }

    @Test
    void logVaccine_subjective_containsConsentCheckboxes() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.logVaccine("Shingrix #1/2");

        // 주관적 기록에 사전 동의 체크 항목이 포함되어야 함
        verify(bridge).insertBlock(anyInt(), argThat(text ->
                text.contains("no allergy") && text.contains("Guillain-Barré")
        ));
    }

    @Test
    void logVaccine_assessmentContainsVaccineName() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.logVaccine("Shingrix #1/2");

        verify(bridge).insertLine(eq(7), argThat(line ->
                line.contains("Shingrix #1/2")
        ));
    }

    @Test
    void logVaccine_planContainsVaccinationText() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.logVaccine("Shingrix #1/2");

        verify(bridge).insertLine(eq(8), argThat(line ->
                line.toLowerCase().contains("vaccination")
        ));
    }

    // ── logVaccine 실패 경로 ──────────────────────────────────────────────

    @Test
    void logVaccine_subjectiveFails_returnsFalse() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(false);
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        assertThat(service.logVaccine("COVID-19")).isFalse();
    }

    @Test
    void logVaccine_assessmentFails_returnsFalse() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(eq(7), anyString())).thenReturn(false);
        when(bridge.insertLine(eq(8), anyString())).thenReturn(true);

        assertThat(service.logVaccine("COVID-19")).isFalse();
    }

    @Test
    void logVaccine_planFails_returnsFalse() {
        when(bridge.insertBlock(anyInt(), anyString())).thenReturn(true);
        when(bridge.insertLine(eq(7), anyString())).thenReturn(true);
        when(bridge.insertLine(eq(8), anyString())).thenReturn(false);

        assertThat(service.logVaccine("COVID-19")).isFalse();
    }
}
