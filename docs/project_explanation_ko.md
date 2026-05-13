# GDSEMR 프로젝트 설명자료

## 1. 프로젝트 개요

GDSEMR은 진료 현장에서 의무기록 작성, 임상 보조 계산, 검사 결과 정리, 진단 코드 검색, AI 기반 문서 초안 생성을 지원하기 위한 EMR(Electronic Medical Record) 프로토타입입니다. 중심 애플리케이션은 JavaFX 기반 데스크톱 EMR이며, 서버 API, LLM 연동 모듈, 모바일 병원 안내 앱, ECG 인공지능 연구 파이프라인이 주변 구성요소로 연결될 수 있도록 구성되어 있습니다.

이 프로젝트의 핵심 목표는 다음과 같습니다.

- 진료 중 반복되는 문서 입력 시간을 줄입니다.
- SOAP 형식의 임상 기록을 구조화합니다.
- EKG, 갑상선, 골밀도, 알레르기, 예방접종, 임상검사 등 진료 보조 모듈을 EMR 입력 흐름에 연결합니다.
- KCD 진단 코드, 약물, 검사 기준값 등 참조 데이터를 빠르게 조회합니다.
- Gemini/OpenAI 같은 LLM을 이용해 임상 문서 초안 작성과 요약을 보조합니다.
- ECG 신호 분석 연구 코드를 통해 향후 심전도 판독 보조 기능으로 확장할 수 있는 기반을 제공합니다.

본 시스템은 현재 프로토타입 및 연구 개발 단계의 소프트웨어입니다. 임상 최종 판단, 진단, 처방은 반드시 의료진의 확인과 책임 하에 이루어져야 합니다.

## 2. 전체 구성

프로젝트는 크게 다음 구성으로 나눌 수 있습니다.

| 구성 | 역할 | 주요 기술 |
| --- | --- | --- |
| `app` | JavaFX 기반 데스크톱 EMR 클라이언트 | Java 25, JavaFX 25, SQLite |
| `server` | 환자/방문/템플릿 API 서버 | Spring Boot, JPA, H2, Flyway |
| `ai` | LLM 서비스 게이트웨이 | Gemini API, OpenAI Responses API, Gson, Java HTTP Client |
| `core` | 공통 설정, DB 경로, 유틸리티 | Java, SQLite runtime path |
| `AppGDS` | 병원 안내용 Android 앱 | Kotlin, Jetpack Compose, FCM 준비 |
| `Kaggo-eEKG2026` | ECG 딥러닝 학습/추론 연구 파이프라인 | Python, PyTorch, PTB-XL, MIT-BIH |

데스크톱 앱은 로컬 SQLite 데이터와 UI 모듈을 이용해 진료 기록을 작성하고, 필요 시 서버 API 및 LLM 모듈과 연결됩니다. 서버는 향후 다중 클라이언트, 중앙 저장소, 병원 시스템 연동으로 확장할 수 있는 백엔드 골격입니다. ECG AI 코드는 EMR 본체와 완전히 통합된 제품 기능이라기보다는, 향후 심전도 분석 보조 기능으로 발전시킬 수 있는 별도 연구 자산입니다.

## 3. 기술적인 측면

### 3.1 JavaFX 기반 EMR 클라이언트

`app` 모듈은 `com.emr.gds.IttiaApp`을 메인 클래스로 하는 JavaFX 애플리케이션입니다. 화면은 진료기록 작성에 필요한 여러 TextArea와 도구 창으로 구성되어 있으며, 주요 입력 영역은 CC, PI, ROS, PMH, S, O, PE, A, P, Comment 같은 임상 기록 섹션으로 나뉩니다.

기술적 특징은 다음과 같습니다.

- JavaFX UI로 데스크톱 환경에서 빠른 반응성과 독립 실행성을 확보합니다.
- TextArea Manager와 Bridge 구조를 통해 보조 창에서 작성한 결과를 EMR의 특정 섹션으로 삽입합니다.
- 약어 확장, 템플릿 삽입, 전체 기록 복사 등 진료실 문서 작업에 필요한 단축 기능을 제공합니다.
- 로그인 후 약어 DB와 주요 데이터를 비동기로 로드하여 초기 화면 지연을 줄이도록 구성되어 있습니다.
- CSS 테마와 FXML 화면을 사용해 기능별 UI를 분리합니다.

### 3.2 로컬 데이터 저장 구조

애플리케이션은 SQLite 기반의 로컬 데이터베이스를 사용합니다. 주요 DB는 다음 용도에 사용됩니다.

- `abbreviations.db`: 약어와 확장 문구 저장
- `prolist.db`: 문제 목록 및 추적 항목 관리
- `emr_templates.db`: 재사용 가능한 EMR 문서 템플릿
- `kcd_database.db`, `KCD-9master_4digit.csv`: 한국표준질병사인분류 코드 참조
- `history.db`: 사용자 입력 이력 및 보조 기록 저장

런타임 데이터는 `~/.gdsemr/db`, 로그는 `~/.gdsemr/logs`에 저장하도록 정리되어 있어, 소스 저장소와 실제 사용자 데이터를 분리하는 방향으로 현대화되어 있습니다.

### 3.3 Spring Boot 서버 API

`server` 모듈은 Spring Boot 기반 REST API 골격입니다. 현재 환자와 방문 정보를 다루는 기본 모델이 구현되어 있습니다.

- `patients`: 이름, 생년월일, 전화번호 등 환자 기본정보
- `visits`: 환자별 방문 시점, 방문 사유, 기록 메모
- JPA Repository 기반 CRUD
- Flyway migration을 통한 DB 스키마 관리
- `/api/v1/templates` 기반 템플릿 API 확장 가능
- `/health` 계열 상태 확인 엔드포인트

현재는 H2와 프로토타입 수준의 API 구조를 사용하지만, 향후 PostgreSQL/MySQL, 인증/권한, 감사 로그, 병원 네트워크 배포 구조로 확장할 수 있습니다.

### 3.4 LLM 연동 구조

`ai` 모듈은 Gemini와 OpenAI를 공통 `AiService` 인터페이스로 감싸는 LLM 게이트웨이입니다.

주요 기능은 다음과 같습니다.

- `GEMINI_API_KEY`, `OPENAI_API_KEY` 환경변수 기반 인증
- 사용 가능한 모델 목록 조회
- 텍스트 입력과 이미지 입력을 함께 전달하는 멀티모달 요청 구조
- Gemini API의 429/503 응답에 대한 재시도 및 exponential backoff
- OpenAI Responses API를 통한 응답 생성
- 건강상태 확인 및 모델 목록 출력용 Gradle task 제공

의료 문서에서는 LLM 결과를 최종 진단이 아니라 “초안”, “요약”, “문장 정리”, “체크리스트 보조”로 제한해 사용하는 설계가 적절합니다. 환자 개인정보가 외부 API로 전송될 수 있으므로 실제 운영 전 비식별화, 동의, 접근권한, 로그 정책이 반드시 필요합니다.

### 3.5 ECG AI 연구 파이프라인

`Kaggo-eEKG2026`에는 PyTorch 기반 ECG 학습 및 추론 코드가 포함되어 있습니다.

기술 요소는 다음과 같습니다.

- PTB-XL 12유도 ECG 5대 superclass 분류: `NORM`, `MI`, `STTC`, `CD`, `HYP`
- MIT-BIH beat pretraining을 통한 박동 형태 특징 학습
- 1D Convolution Residual Block 기반 lead encoder
- lead별 feature를 평균 pooling과 attention pooling으로 결합
- class-wise threshold tuning
- macro PR-AUC, ROC-AUC, F1, recall, specificity, balanced accuracy 평가
- 부정맥 정밀 분류를 위한 LSTM 기반 specialist 모델 실험
- PDF ECG 이미지에서 12유도 신호를 복원하고 품질 점수와 판독 초안을 생성하는 연구 workflow

이 영역은 의료기기 수준의 검증이 완료된 진단 엔진이 아니라 연구용 분석 파이프라인입니다. 실제 의료 활용을 위해서는 외부 검증 데이터, 전향적 검증, 판독자 비교, 오류 분석, 규제 대응이 필요합니다.

### 3.6 Android 병원 안내 앱

`AppGDS`는 환자 대상 병원 안내용 Android 앱입니다.

- Kotlin + Jetpack Compose 기반
- 진료시간, 공지사항, 전화 연결, 길찾기 등 단순 안내 기능
- 고령 환자도 사용하기 쉬운 큰 글씨와 단순 화면 지향
- Firebase Cloud Messaging을 통한 공지/휴진 알림 확장 가능
- Naver/Kakao map key 자리 준비

EMR 본체와 직접적인 진료기록 기능을 공유하기보다는, 환자 커뮤니케이션 채널로 활용할 수 있는 보조 앱입니다.

## 4. 의학적인 이용 측면

### 4.1 외래 진료 문서화 보조

GDSEMR의 가장 직접적인 활용 분야는 외래 진료 기록 작성입니다. 의료진은 환자의 주호소, 현병력, 과거력, 검토계통, 진찰소견, 평가, 계획을 구조화된 TextArea에 입력하고, 약어 확장과 템플릿 기능으로 반복 문구 입력을 줄일 수 있습니다.

예상 효과는 다음과 같습니다.

- 진료 중 타이핑 부담 감소
- SOAP 형식 유지
- 누락되기 쉬운 문진 항목의 체크리스트화
- 환자 설명서, 의뢰서, 추적계획 문구 작성 시간 단축
- 의무기록 표현의 일관성 향상

### 4.2 질환별 진료 보조 모듈

프로젝트에는 여러 진료 보조 모듈이 포함되어 있습니다.

| 모듈 | 의학적 활용 |
| --- | --- |
| 갑상선 모듈 | TSH, Free T4, Free T3, Tg, TgAb, TRAb 등 결과 정리, 갑상선 기능저하/항진/암 추적 기록, TI-RADS 관련 요약 |
| EKG 모듈 | 심전도 판독 문구를 Objective 영역에 삽입, 단순 판독 보고서 형식화 |
| CXR 모듈 | 흉부 X-ray 리뷰 및 판독 보조 문구 작성 |
| 골밀도 모듈 | T-score/Z-score 기반 골감소증, 골다공증, 중증 골다공증 평가 문구 생성 |
| 알레르기 모듈 | 증상, 원인 물질, 아나필락시스 부인 여부를 구조화해 PMH에 삽입 |
| 예방접종 모듈 | 백신 관련 상담과 부작용 기록 보조 |
| 임상검사 모듈 | 검사 항목과 기준값 참조, 기록 보조 |
| 약물 모듈 | 약물 카테고리별 조회와 기록 작성 보조 |
| KCD 모듈 | 한국 질병분류 코드 검색 및 진단명 참조 |

특히 내분비·대사질환 외래에서는 갑상선 질환, 당뇨 관련 HbA1c, 비만/BMI, 골다공증, 예방접종, 알레르기 문진이 반복적으로 등장하므로, 이러한 모듈은 진료 흐름을 단축하는 실용적 가치가 있습니다.

### 4.3 ECG 분석 연구 활용

ECG AI 파이프라인은 다음과 같은 연구 및 향후 임상 보조 기능의 기반이 될 수 있습니다.

- 12유도 ECG의 정상/심근경색/전도장애/비대/ST-T 변화 분류 연구
- 부정맥 특화 모델 개발
- PDF 또는 이미지 형태의 ECG를 디지털 신호로 복원하는 workflow 검토
- 모델 confidence와 digitization quality를 함께 제시하여 판독 가능성 평가
- EMR에 들어갈 ECG 요약 초안 생성

의료적으로 중요한 점은 ECG AI 결과를 단독 진단으로 쓰지 않는 것입니다. ECG는 신호 품질, lead placement, 환자 상태, 기존 병력, 증상, 혈액검사, 영상검사와 함께 해석되어야 합니다. 따라서 이 기능은 “판독 보조”, “우선순위 표시”, “보고서 초안”으로 제한해 사용하는 것이 적절합니다.

### 4.4 임상 의사결정 지원의 방향

이 프로젝트의 임상적 가치는 의료진을 대체하는 자동 진단보다, 의료진의 기록·검색·요약·검토 작업을 줄이는 데 있습니다.

적절한 사용 예시는 다음과 같습니다.

- 환자 방문 전 기존 문제목록과 추적계획 확인
- 진료 중 문진 체크리스트 기반 누락 방지
- 검사 수치와 기준값을 함께 보여주어 해석 보조
- 진료 후 환자 설명 문구 또는 요약 문서 초안 생성
- 진단 코드 검색으로 청구/통계/의무기록 표준화 지원
- 반복 외래 환자의 추적 항목을 일관되게 기록

부적절한 사용 예시는 다음과 같습니다.

- AI 출력만 근거로 진단 또는 처방 결정
- 개인정보 비식별화 없이 외부 LLM API에 전체 의무기록 전송
- 검증되지 않은 ECG 모델 결과를 확정 판독으로 기재
- 병원 인증/권한/감사 로그 없이 여러 사용자가 실제 환자 데이터를 공유

## 5. 보안, 개인정보, 규제 고려사항

실제 의료기관에서 사용하려면 다음 요소가 추가되어야 합니다.

- 사용자 인증과 역할 기반 접근제어
- 환자 개인정보 암호화 및 백업 정책
- 외부 LLM API 전송 전 비식별화 또는 병원 내부 모델 사용
- 접근 로그, 수정 이력, 감사 추적
- 데이터베이스 백업/복구 절차
- 병원 OCS/EMR/PACS/LIS와의 표준 연동 검토
- 의료기기 소프트웨어 해당 여부 검토
- 임상 검증 계획과 책임 소재 명확화

특히 LLM과 ECG AI 기능은 의료적 위험도가 있으므로 “의료진 검토 필수”라는 사용 원칙을 UI와 보고서에 명확히 표시하는 것이 바람직합니다.

## 6. 개발 및 실행 개요

필수 환경은 다음과 같습니다.

- JDK 25
- Gradle Wrapper
- JavaFX 25
- SQLite JDBC
- LLM 기능 사용 시 `GEMINI_API_KEY` 또는 `OPENAI_API_KEY`

주요 실행 명령은 다음과 같습니다.

```bash
./gradlew run
./gradlew runServer
./gradlew healthCheck --no-daemon
./gradlew listModels --no-daemon
```

ECG 연구 파이프라인은 별도 Python 환경에서 실행합니다.

```bash
python -m venv ecg_training/.venv
source ecg_training/.venv/bin/activate
pip install -r ecg_training/requirements.txt
python -m ecg_training.train_ptbxl --config ecg_training/configs/ptbxl_baseline.json
```

## 7. 기대 효과

기술적으로는 JavaFX 데스크톱 앱, Spring Boot API, SQLite 로컬 저장소, LLM 게이트웨이, PyTorch ECG 연구 파이프라인을 한 프로젝트 안에서 단계적으로 통합할 수 있는 구조를 갖추고 있습니다.

의학적으로는 진료기록 작성 자동화, 질환별 체크리스트, 검사 결과 정리, 진단 코드 검색, ECG 판독 보조 연구를 통해 외래 진료의 반복 업무를 줄이고 기록 품질을 높이는 방향의 활용이 가능합니다.

최종적으로 GDSEMR은 “의사의 판단을 대체하는 시스템”이 아니라 “의사가 더 빠르고 일관되게 기록하고 검토하도록 돕는 임상 업무 보조 시스템”으로 정의하는 것이 가장 적절합니다.
