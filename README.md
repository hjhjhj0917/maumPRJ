# MAUM (마음) Backend

일기 텍스트를 분석해 감정·우울 지수를 추적하고, RAG 챗봇으로 정신건강기관/청년정책 정보를 안내하는 서비스의 Spring Boot 백엔드입니다.

**핵심 설계 원칙**: 이 백엔드는 "AI 서버 호출이 느려도 DB 커넥션과 트랜잭션을 물고 있지 않는다"를 원칙으로 합니다. 일기 저장 같은 쓰기 작업은 먼저 DB에 반영한 뒤 별도로 AI 분석을 호출하고, 챗봇 스트리밍 응답은 WebFlux(`Flux<String>`)로 비동기 처리해 커넥션을 점유하지 않습니다. 또한 모든 비즈니스 예외는 `ResponseEntity`의 실제 HTTP 상태 코드로 내려가야 한다는 원칙을 가지고 있습니다 — 과거 모든 응답이 `200 OK`로 나가 프론트에서 실패를 성공으로 처리하던 버그를 겪은 뒤 `GlobalExceptionHandler`/`AuthExceptionHandler`에서 상태 코드를 명확히 분리했습니다.

* **버전**: 0.0.1-SNAPSHOT
* **대상 환경**: 로컬/개인 개발 환경 (MariaDB + MongoDB + Redis + GCS + maumPy AI 서버 연동)
* **실행 환경**: Java 17, Spring Boot 4.0, 기본 포트 `8080`

---

## 목차

1. [주요 기능](#주요-기능)
2. [핵심 아키텍처](#핵심-아키텍처)
3. [핵심 로직/규칙](#핵심-로직규칙)
4. [시작하기](#시작하기)
5. [DB/외부 서비스 준비 사항](#db외부-서비스-준비-사항)
6. [프로젝트 구조](#프로젝트-구조)
7. [기술 스택](#기술-스택)
8. [테스트](#테스트)
9. [주요 변경 이력](#주요-변경-이력)

---

## 주요 기능

### 1. 인증/계정 (`LoginController`, `UserRegController`, `UserInfoController`)
- 아이디/비밀번호 로그인(`loginProc`) 시 `AuthenticationManager`로 인증 후 JWT 액세스/리프레시 토큰을 발급(`IJwtTokenService`)합니다.
- 액세스 토큰 만료 시 리프레시 토큰으로 재발급하는 `/refresh` 엔드포인트를 제공합니다.
- 로그아웃/회원탈퇴 시 남은 토큰 만료 시간만큼 Redis 블랙리스트에 등록해 토큰을 즉시 무효화합니다.
- 회원가입 시 아이디 중복 확인, 이메일 인증 코드 발송/검증, 비밀번호 재설정, 프로필 이미지·주소 변경, 회원 탈퇴 API를 제공합니다.

### 2. 일기 CRUD 및 AI 분석 (`DiaryController`)
- 일기 작성/수정 시 AI 서버(maumPy)에 감정 분석 + 우울증 예측 + AI 요약 + 감정 기반 음악 추천을 한 번의 호출로 요청하고 결과를 저장합니다.
- 타이핑 중 자동 저장을 위한 `draftSave`(AI 분석 없이 제목/본문만 저장)를 별도 제공합니다.
- 월별 목록(`/monthly`), 상세 조회, 키워드 검색(`/search`), 감정색 필터(`/filter`), 최근 목록(`/recent`), 즐겨찾기 토글/목록, 제목 인라인 수정, 상단 고정(pin) API를 제공합니다.
- 일기당 최대 3장까지 이미지를 GCS에 업로드/삭제하는 API(`/{diaryNo}/images/upload`, `/images/delete`)를 제공합니다.

### 3. RAG 기반 챗봇 (`ChatBotController`)
- `/stream`에서 SSE(`Flux<String>`)로 AI 서버의 답변을 스트리밍 전달합니다.
- 채팅방 생성/목록/이름변경/고정/삭제 등 멀티 채팅방 관리 API를 제공합니다.
- 채팅방별 메시지 이력 조회(`/rooms/{chatRoomNo}/messages`), 과거 메시지에 대한 TTS 음성 재생성(`/messages/{chatMsgNo}/tts`)을 제공해 재방문 시에도 음성을 다시 들을 수 있습니다.

### 4. 음성 인식 (`SttController`)
- 녹음 파일을 멀티파트로 받아 AI 서버에 프록시해 텍스트로 변환합니다.

### 5. 상담기관 지도 (`MapController`)
- MongoDB에 저장된 정신건강 상담기관 좌표 데이터를 DTO로 변환해 제공합니다(엔티티/도큐먼트 직접 반환 금지).

### 6. 마이페이지 통계 & 주간 리포트 (`DiaryController`)
- 감정별 통계(`/emotions/stats`), 총 작성 수·연속 작성일 등 요약 통계(`/stats/summary`), 최근 6개월 우울 지수 추이(`/stats/trend`), 가장 많이 추천된 음악 Top5(`/stats/top-music`)를 조회합니다.
- 최근 일주일 일기를 모아 Gemini로 요약 코멘트를 생성하는 주간 리포트(`/stats/report`)를 제공하며, 호출 비용 절감을 위해 Redis에 캐싱합니다.

### 7. 자동화 (스케줄러)
- `DataUpdateScheduler`: 공공데이터(상담기관 등)를 주기적으로 최신화합니다.
- `UserCleanupScheduler`: 탈퇴 처리된 유저 데이터를 주기적으로 정리합니다.

---

## 핵심 아키텍처

### JWT + Redis 인증 구조
- Spring Security OAuth2 Resource Server 기반으로 JWT를 검증하며, `CookieOrHeaderBearerTokenResolver`가 쿠키/헤더 양쪽에서 토큰을 읽어옵니다.
- 로그아웃/탈퇴 시 남은 만료 시간만큼 Redis에 블랙리스트로 등록하고, `RedisBlacklistFilter`가 매 요청마다 블랙리스트 여부를 검사해 무효화된 토큰을 즉시 차단합니다.
- 액세스 토큰 만료 시 `refreshToken` 쿠키로 재발급받는 구조로, 프론트에서 매번 재로그인하지 않도록 합니다.

### 비즈니스 예외와 HTTP 상태 코드 분리
- `GlobalExceptionHandler`가 `IllegalArgumentException`→`400`, `OptimisticLockException`(동시 수정 충돌)→`409`로 매핑합니다.
- `AuthExceptionHandler`가 `AuthenticationException` 계열(`BadCredentialsException`, `LockedException`, `DisabledException` 등)을 `401`로 매핑하고 사용자 메시지로 변환합니다.
- 과거에는 컨트롤러가 항상 `ResponseEntity.ok(...)`로 감싸 실패 응답도 HTTP 200으로 나가는 문제가 있었고, 이를 예외 핸들러 분리로 해결했습니다.

### AI 서버 연동 시 커넥션 점유 방지
- 일기 분석처럼 동기 호출이 필요한 경우는 DB 트랜잭션을 먼저 커밋한 뒤 AI 서버를 호출해, 느린 외부 API 응답이 DB 커넥션을 오래 잡고 있지 않도록 합니다.
- 챗봇 응답처럼 응답 시간이 길고 점진적으로 내려오는 경우는 WebFlux의 `Flux<String>`과 SSE(`text/event-stream`)로 스트리밍해, 서버 스레드/커넥션을 블로킹하지 않습니다.

---

## 핵심 로직/규칙

| 규칙 | 내용 |
|---|---|
| `CommonResponse<T>` 응답 규약 | 모든 API는 `{ httpStatus, message, data }` 형태의 `CommonResponse`로 감싸 응답. `CommonResponse.of(status, message, data)` 정적 팩토리로 생성 |
| 예외 → 상태 코드 매핑 | `IllegalArgumentException` → 400, `OptimisticLockException` → 409, `AuthenticationException` 계열 → 401. 그 외 처리되지 않은 예외는 Spring 기본 핸들링을 따름 |
| 일기 저장 vs 임시저장 | `diaryInsert`/`diaryUpdate`는 AI 분석(감정/우울/요약/음악추천)까지 수행, `draftSave`는 제목/본문만 저장해 타이핑 중 불필요한 AI 호출을 방지 |
| 본인 소유 검증 | 즐겨찾기/고정/제목 수정 등 단건 수정 API는 `userNo` 기준으로 영향받은 row 수를 확인해, 0건이면 "본인의 일기만 변경할 수 있거나, 존재하지 않는 일기입니다" 예외 발생 |
| DTO 네이밍 | 요청/중간 가공 DTO는 `pDTO`(parameter), 응답 DTO는 `rDTO`(result)/`rList`, 원본 입력 DTO는 역할별로 `dDTO`/`sDTO`/`uDTO` 등으로 구분해 로그와 함께 추적 |
| 식별 마커 주석 | 최근 추가/수정된 코드에는 `★ 즐겨찾기 이후 추가/수정` 같은 식별 마커 주석을 남겨, 리뷰 시점의 변경 범위를 구분 |

---

## 시작하기

### 요구 사항
- Java 17 (JDK)
- MariaDB, MongoDB, Redis
- 함께 실행되는 [maumPy](https://github.com/hjhjhj0917/maumPy) AI 서버 (`localhost:8000` 기준)
- GCP 프로젝트 (Cloud Storage — 일기 이미지 업로드용)

### 설정
`src/main/resources/application-secret.yaml`에 DB/Redis/메일/JWT/GCS 등 민감한 연동 정보를 구성해야 합니다. 이 파일은 `.gitignore`에 포함되어 저장소에는 올라가지 않으므로, 값은 별도로 안전하게 전달받아 각자 환경에 맞게 구성해야 합니다. (저장소에는 실제 키 이름/값 구조를 공개하지 않습니다.)

### 빌드/실행
```bash
./gradlew build
./gradlew bootRun
```
서버는 기본적으로 `8080` 포트에서 실행되며, [maumReact](https://github.com/hjhjhj0917/maumReact) 개발 서버(`vite.config.js`의 프록시)가 이 포트를 바라보도록 구성되어 있습니다.

---

## DB/외부 서비스 준비 사항

| 구성 요소 | 용도 |
|---|---|
| MariaDB | 사용자 계정, 일기 본문/메타데이터, 채팅방/메시지 등 정형 데이터 |
| MongoDB | 상담기관/정책 등 비정형 문서 데이터 및 벡터 검색 |
| Redis | 로그인 세션/블랙리스트 토큰 관리, 통계·주간 리포트 캐싱 |
| GCS (Google Cloud Storage) | 일기 첨부 이미지, 프로필 이미지 저장 |
| maumPy (FastAPI AI 서버) | 감정/우울증 분석, RAG 챗봇 응답, STT/TTS, 음악 추천 — 환경변수로 주소 설정 필요 |
| 메일 서버(SMTP) | 회원가입/비밀번호 재설정/회원탈퇴 시 이메일 인증 코드 발송 |

모든 접속 정보(호스트/계정/키)는 `application-secret.yaml`(git에 포함되지 않음)로 주입하며, 이 README에는 구체적인 키 이름이나 값 구조를 기재하지 않습니다.

---

## 프로젝트 구조

```text
src/main/java/com/example/maum/
 ├── auth/            # 인증 관련 권한(UserRole), 인증 주체 정보(AuthInfo)
 ├── config/          # JWT, QueryDSL, Redis, Security, RestClient 등 환경 설정
 ├── controller/      # ChatBot, Diary, Login, Map, Stt, UserInfo, UserReg API 엔드포인트
 │    ├── exception/  # AuthExceptionHandler (인증 예외 처리)
 │    └── response/   # 공통 응답 형식(CommonResponse)
 ├── dto/             # ChatBot/ChatRoom/ChatMessage, Diary(+Image/Music/Stats), UserInfo 등 DTO
 ├── jwt/             # CookieOrHeaderBearerTokenResolver 등 토큰 처리 로직
 ├── repository/      # MariaDB(JPA)/MongoDB 데이터 접근 인터페이스
 │    ├── entity/     # DB 엔티티 및 Mongo 도큐먼트 객체
 │    └── projection/ # 통계용 native 쿼리 결과 매핑 프로젝션
 ├── scheduler/       # DataUpdateScheduler, UserCleanupScheduler
 ├── security/        # RedisBlacklistFilter 등 보안 필터
 ├── service/         # 도메인별 핵심 비즈니스 로직 인터페이스
 │    └── impl/       # 실제 구현체 (ChatBotService, DiaryService, GcsService, SttService 등)
 └── util/            # CmmUtil, DateUtil, EncryptUtil, EmotionColorMapper
```

---

## 기술 스택

| 영역 | 기술 |
|---|---|
| 언어/프레임워크 | Java 17, Spring Boot 4.0 |
| 데이터 접근 | Spring Data JPA, QueryDSL 5.0, MyBatis, Spring Data MongoDB |
| 인증/보안 | Spring Security (OAuth2 Resource Server), JWT |
| 캐시/세션 | Redis, Ehcache |
| 비동기/스트리밍 | Spring WebFlux (챗봇 SSE 스트리밍) |
| 스토리지 | Google Cloud Storage (`google-cloud-storage`) |
| DB | MariaDB, MongoDB |
| 기타 | Jsoup(HTML 파싱), Spring Mail, Lombok |
| 빌드 도구 | Gradle |
| AI 서버 연동(별도 저장소) | [maumPy](https://github.com/hjhjhj0917/maumPy) — FastAPI, Google Gemini(RAG/요약), KoELECTRA(감정 분석), klue/roberta-base(우울증 분석) |
| 프론트엔드(별도 저장소) | [maumReact](https://github.com/hjhjhj0917/maumReact) — React |

---

## 테스트

```bash
./gradlew test
```
JUnit 5(`junit-platform-launcher`) 기반으로 `spring-boot-starter-test`, `mybatis-spring-boot-starter-test`를 사용합니다.

---

## 주요 변경 이력

- JWT 토큰 기반 로그인/회원가입, accessToken 만료 시 refreshToken 재발급 로직 구축
- 카카오 지도 기반 주변 상담기관 검색/표시 기능 추가
- 일기 작성 시 AI 분석 서버 연동(감정/우울증 분석 결과 저장) 및 분석 결과 반영 로직 추가
- 마이페이지 회원정보/프로필 이미지 수정, 회원 탈퇴 기능 구현
- RAG 챗봇 멀티턴 대화 히스토리, TTS 응답 처리 추가
- 채팅방 다중 생성/관리 및 일기·채팅방 사이드바 관리(고정/이름변경/삭제) 기능 추가
- STT API 프록시, 일기 이미지 업로드(GCS), 일기 음악 추천 결과 저장 기능 추가
- 일기 즐겨찾기 기능 및 감정 필터 검색 추가
- 마이페이지 통계 위젯(연속 작성일/우울 지수 추이/인기 추천곡/주간 리포트) API 추가
- 지도 API에서 MongoDB 문서 엔티티를 직접 반환하지 않고 DTO로 변환하도록 리팩터링
- 비즈니스 예외 응답이 항상 200 OK로 나가 프론트에서 실패를 성공으로 처리하던 문제 수정
- 챗봇 과거 내역 재진입 시 TTS 음성을 다시 들을 수 있는 기능 추가
- HyperClova X → Google Gemini 전환 완료 (감정 분석/요약/RAG 응답 생성 모델 교체)
