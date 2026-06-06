# GDSEMR 개발 규칙 (CLAUDE.md)

## Project Overview
- Java 기반 의료 전자차트 시스템 (GDSEMR_ver_1.1001)
- JavaFX + SQLite 중심
- 완료 모듈: allergy, history(헥사고날), vaccine, ekg, thyroid, kcd, clinicalLab

## 핵심 규칙 요약

### 1. 예외 처리 정책
- **Repository**: SQLException → 도메인 예외 변환 후 throw, `logger.error("msg", e)`
- **Service**: 복구 불가 시 RuntimeException wrap + `logger.error`
- **Controller/UI**: 사용자 Alert + `logger.warn/error`
- **절대 금지**: `e.printStackTrace()`, `System.err.println`, 빈 catch 블록
- Logger 사용 시 반드시 예외 객체를 마지막 인자로 전달 (`logger.error("msg: {}", ctx, e)`)

### 2. DB 연결 정책
- **사용자 데이터 DB** (`abbreviations`, `history`, `plan_history` 등):
  - `AppDatabaseManager.getInstance().getXxxConnection()` 사용
  - 커넥션은 **절대 닫지 않음** (공유 커넥션)
  - `Statement`, `ResultSet`만 try-with-resources로 닫기
- **앱 참조 데이터 DB** (`med_data`, `kcd_database` 등):
  - `AppDatabaseManager.resolveAppDbPath("filename.db")` 사용
  - 호출마다 새 커넥션 허용

### 3. 패키지 구조 (Feature 모듈)
