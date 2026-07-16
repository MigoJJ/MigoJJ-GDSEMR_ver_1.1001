package com.emr.gds.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RuntimeEnvironment 테스트")
class RuntimeEnvironmentTest {

    @Test
    @DisplayName("기본 데이터 디렉토리 반환")
    void testGetBaseDataDirectory() {
        Path baseDir = RuntimeEnvironment.getBaseDataDirectory();
        
        assertThat(baseDir).isNotNull();
        assertThat(baseDir.toString()).contains(".gdsemr");
    }

    @Test
    @DisplayName("데이터베이스 디렉토리 반환")
    void testGetDatabaseDirectory() {
        Path dbDir = RuntimeEnvironment.getDatabaseDirectory();
        
        assertThat(dbDir).isNotNull();
        assertThat(dbDir.toString()).contains("db");
    }

    @Test
    @DisplayName("로그 디렉토리 반환")
    void testGetLogDirectory() {
        Path logDir = RuntimeEnvironment.getLogDirectory();
        
        assertThat(logDir).isNotNull();
        assertThat(logDir.toString()).contains("logs");
    }

    @Test
    @DisplayName("데이터베이스 경로 해석")
    void testResolveDatabasePath() {
        Path dbPath = RuntimeEnvironment.resolveDatabasePath("test.db");
        
        assertThat(dbPath).isNotNull();
        assertThat(dbPath.getFileName().toString()).isEqualTo("test.db");
    }

    @Test
    @DisplayName("무효한 파일명 처리")
    void testResolveDatabasePathWithInvalidName() {
        assertThatThrownBy(() -> RuntimeEnvironment.resolveDatabasePath(null))
            .isInstanceOf(NullPointerException.class);
    }
}
