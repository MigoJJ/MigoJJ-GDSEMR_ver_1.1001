# GDSEMR 개발 규칙

## 예외 처리 정책

| 레이어 | 규칙 |
|---|---|
| **Repository** | `SQLException` → 도메인 예외로 변환 후 throw. `logger.error("설명", e)` 로 기록. |
| **Service** | 복구 불가 시 `RuntimeException` 계열로 wrap. `logger.error` 사용. |
| **Controller / Stage (UI)** | 사용자에게 `Alert` 표시 + `logger.warn` 또는 `logger.error`. |
| **공통 금지** | `e.printStackTrace()` / `System.err.println` / 빈 catch `{}` 사용 금지. |

예외를 logger에 기록할 때 반드시 예외 객체를 세 번째 인자로 전달해야 스택트레이스가 로그에 남습니다:
```java
// 올바름
logger.error("설명: {}", context, e);

// 금지
e.printStackTrace();
System.err.println(e.getMessage());
```

---

## DB 연결 정책

**사용자 데이터 DB** (`abbreviations`, `history`, `references`, `plan_history`, `prolist`):
- `AppDatabaseManager.getInstance().getXxxConnection()` 사용.
- 반환된 커넥션은 공유 커넥션이므로 **절대 닫지 않음**.
- `Statement`, `ResultSet`만 try-with-resources로 닫음.

**앱 참조 데이터 DB** (`med_data`, `ClinicalLabItems`, `kcd_database`, `emr_templates`):
- `AppDatabaseManager.resolveAppDbPath("filename.db")` 로 경로 해결.
- 호출마다 새 커넥션 사용 가능 (이 DB들은 자체 캐싱 또는 단발성 접근).

---

## 패키지 구조 표준 (feature 모듈)

```
features/{module}/
├── {Module}Action.java      ← 외부 진입점 (public API, 루트 유지)
├── model/                   ← 순수 데이터 클래스, 상수 (JavaFX 없음)
├── service/                 ← 비즈니스 로직 (테스트 가능)
└── view/                    ← JavaFX UI (Stage, Controller, Pane)
```

**완료된 모듈:** `allergy`, `history`(헥사고날), `vaccine`, `ekg`, `thyroid`, `kcd`, `clinicalLab`

---

## 로거 선언 패턴

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
```

- 모든 클래스에 선언. `java.util.logging.Logger` 절대 금지.
- SLF4J(`org.slf4j.Logger`) + Logback 백엔드 사용.
- CLI 도구(`GeminiHealthCheck`, `ModelListPrinter`, `CsvToSqliteImporter`)는
  stdout 진행 상태에 `System.out.println` 허용, stderr 오류는 logger 사용.

---

## 테스트 작성 원칙

- **Repository**: 인메모리 SQLite (`jdbc:sqlite::memory:`) + package-private 생성자 seam
- **Service**: Mockito mock + AssertJ assertions
- **의료 로직**: 경계값(boundary) 케이스 필수 포함
- **JavaFX UI 클래스**: 단위 테스트 불필요 — 통합 테스트 대상

**테스트 JVM 인수** (build.gradle.kts):
```kotlin
jvmArgs(
    "-XX:+EnableDynamicAgentLoading",
    "-Dnet.bytebuddy.experimental=true",
    "--add-opens=java.base/java.lang=ALL-UNNAMED"
)
```
Java 25 + Mockito 5.14.2 조합 필수. (Mockito 5.11.0 이하 ByteBuddy 호환 불가)

---

## Import 규칙

- `java.sql.*` / `java.util.*` 와일드카드 **금지** → 명시적 import 사용
- `javafx.scene.control.*` 는 컨트롤이 다수일 때 예외적으로 허용
- 미사용 import는 즉시 제거
