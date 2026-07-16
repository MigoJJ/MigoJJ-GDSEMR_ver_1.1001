package com.emr.gds.features.bone.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReferenceRegion 부위명 정규화 테스트")
class ReferenceRegionTest {

    @ParameterizedTest
    @DisplayName("요추 L1-L4 부위명 매칭")
    @CsvSource({
            "L1-L4,LUMBAR_SPINE_L1_L4",
            "l1-l4,LUMBAR_SPINE_L1_L4",
            "Lumbar,LUMBAR_SPINE_L1_L4",
            "lumbar spine L1-L4,LUMBAR_SPINE_L1_L4",
            "L1_L4,LUMBAR_SPINE_L1_L4"
    })
    void testLumbarSpineMatching(String input, String expected) {
        ReferenceRegion result = ReferenceRegion.match(input);
        assertThat(result).isEqualTo(ReferenceRegion.valueOf(expected));
    }

    @ParameterizedTest
    @DisplayName("대퇴경부 부위명 매칭")
    @CsvSource({
            "Neck,FEMORAL_NECK",
            "neck,FEMORAL_NECK",
            "Femoral Neck,FEMORAL_NECK",
            "FN,FEMORAL_NECK",
            "fn,FEMORAL_NECK"
    })
    void testFemoralNeckMatching(String input, String expected) {
        ReferenceRegion result = ReferenceRegion.match(input);
        assertThat(result).isEqualTo(ReferenceRegion.valueOf(expected));
    }

    @ParameterizedTest
    @DisplayName("전체 고관절 부위명 매칭")
    @CsvSource({
            "Total Hip,TOTAL_HIP",
            "total hip,TOTAL_HIP",
            "TH,TOTAL_HIP",
            "th,TOTAL_HIP"
    })
    void testTotalHipMatching(String input, String expected) {
        ReferenceRegion result = ReferenceRegion.match(input);
        assertThat(result).isEqualTo(ReferenceRegion.valueOf(expected));
    }

    @ParameterizedTest
    @DisplayName("기준 부위가 아닌 영역")
    @CsvSource({
            "L1,OTHER",
            "L2,OTHER",
            "L3,OTHER",
            "L4,OTHER",
            "Ward,OTHER",
            "Wards,OTHER",
            "Troch,OTHER",
            "Shaft,OTHER",
            "Unknown,OTHER",
            "random text,OTHER"
    })
    void testNonReferenceMatching(String input, String expected) {
        ReferenceRegion result = ReferenceRegion.match(input);
        assertThat(result).isEqualTo(ReferenceRegion.valueOf(expected));
    }

    @Test
    @DisplayName("null 입력 처리")
    void testNullInput() {
        assertThat(ReferenceRegion.match(null)).isEqualTo(ReferenceRegion.OTHER);
    }

    @Test
    @DisplayName("공백 입력 처리")
    void testBlankInput() {
        assertThat(ReferenceRegion.match("")).isEqualTo(ReferenceRegion.OTHER);
        assertThat(ReferenceRegion.match("   ")).isEqualTo(ReferenceRegion.OTHER);
    }
}
