package com.emr.gds.features.ekg;

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
class EkgReportServiceTest {

    @Mock
    private EmrBridgeService bridge;

    private EkgReportService service;

    @BeforeEach
    void setUp() {
        service = new EkgReportService(bridge);
    }

    // ── formatReport ─────────────────────────────────────────────────────

    @Test
    void formatReport_containsEkgReportLabel() {
        String result = service.formatReport("Normal sinus rhythm");
        assertThat(result).contains("EKG Report");
    }

    @Test
    void formatReport_containsTodayDate() {
        String today = LocalDate.now().toString(); // yyyy-MM-dd
        String result = service.formatReport("Normal sinus rhythm");
        assertThat(result).contains(today);
    }

    @Test
    void formatReport_trimsPaddingFromRawText() {
        String result = service.formatReport("  Normal sinus rhythm  ");
        assertThat(result).contains("Normal sinus rhythm");
        assertThat(result).doesNotContain("  Normal"); // 앞 공백이 제거되어야 함
    }

    @Test
    void formatReport_emptyInput_stillContainsHeader() {
        String result = service.formatReport("   ");
        assertThat(result).contains("EKG Report");
    }

    // ── pushToEmr ────────────────────────────────────────────────────────

    @Test
    void pushToEmr_bridgeReturnsTrue_returnsTrue() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(true);
        assertThat(service.pushToEmr("formatted report")).isTrue();
    }

    @Test
    void pushToEmr_bridgeReturnsFalse_returnsFalse() {
        when(bridge.insertLine(anyInt(), anyString())).thenReturn(false);
        assertThat(service.pushToEmr("formatted report")).isFalse();
    }

    @Test
    void pushToEmr_delegatesToBridgeWithTargetArea() {
        when(bridge.insertLine(eq(5), anyString())).thenReturn(true);
        service.pushToEmr("formatted report");
        // 목표 영역(index 5 = objective)으로 전달했는지 검증
        verify(bridge, times(1)).insertLine(eq(5), anyString());
    }
}
