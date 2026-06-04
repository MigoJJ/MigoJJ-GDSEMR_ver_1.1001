package com.emr.gds.features.imaging.service;

import com.emr.gds.infrastructure.service.EmrBridgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChestXrayServiceTest {

    @Mock
    private EmrBridgeService bridge;

    private ChestXrayService service;

    @BeforeEach
    void setUp() {
        service = new ChestXrayService(bridge);
    }

    // ── pushReport — 입력 검증 ────────────────────────────────────────────

    @Test
    void pushReport_nullBody_returnsFalse_noBridgeCall() {
        assertThat(service.pushReport(null)).isFalse();
        verifyNoInteractions(bridge);
    }

    @Test
    void pushReport_emptyBody_returnsFalse_noBridgeCall() {
        assertThat(service.pushReport("")).isFalse();
        verifyNoInteractions(bridge);
    }

    @Test
    void pushReport_whitespaceOnly_returnsFalse_noBridgeCall() {
        assertThat(service.pushReport("   \n\t  ")).isFalse();
        verifyNoInteractions(bridge);
    }

    // ── pushReport — 성공 경로 ────────────────────────────────────────────

    @Test
    void pushReport_validBody_delegatesToBridge() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.pushReport("No active cardiopulmonary disease.");

        verify(bridge, times(1)).insertLine(anyInt(), anyString());
    }

    @Test
    void pushReport_bridgeReturnsTrue_returnsTrue() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        assertThat(service.pushReport("Normal CXR")).isTrue();
    }

    @Test
    void pushReport_bridgeReturnsFalse_returnsFalse() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(false);

        assertThat(service.pushReport("Normal CXR")).isFalse();
    }

    // ── pushReport — 보고서 형식 ───────────────────────────────────────────

    @Test
    void pushReport_containsChestPaHeader() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.pushReport("Normal CXR");

        verify(bridge).insertLine(anyInt(), argThat(report ->
                report.contains("CHEST PA")
        ));
    }

    @Test
    void pushReport_containsTodayDate() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);
        String today = LocalDate.now().toString();

        service.pushReport("Normal CXR");

        verify(bridge).insertLine(anyInt(), argThat(report ->
                report.contains(today)
        ));
    }

    @Test
    void pushReport_trimsPaddingFromBody() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);

        service.pushReport("  Normal CXR  ");

        verify(bridge).insertLine(anyInt(), argThat(report ->
                report.contains("Normal CXR") && !report.contains("  Normal CXR  ")
        ));
    }

    @Test
    void pushReport_targetsObjectiveArea_index5() {
        when(bridge.insertLine(eq(5), anyString())).thenReturn(true);

        service.pushReport("Normal CXR");

        verify(bridge).insertLine(eq(5), anyString());
    }
}
