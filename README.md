# MAUM (마음) — 일기 기반 AI 감정 분석 서비스

사용자의 일기 텍스트를 기반으로 감정과 우울증 수치를 분석하고, RAG(검색 증강 생성) 기술을 활용해 개인화된 챗봇 상담을 제공하는 웹 서비스입니다.

MAUM은 3개 저장소로 구성됩니다.

| 저장소 | 역할 |
|---|---|
| **maumProject (현재 저장소)** | Spring Boot 백엔드 — 인증, 일기/채팅 API, AI 서버로의 프록시 |
| [maumPy](https://github.com/hjhjhj0917/maumPy) | FastAPI AI 서버 — 감정/우울증 분석, RAG 챗봇, STT/TTS, 음악 추천 |
| [maumReact](https://github.com/hjhjhj0917/maumReact) | React 프론트엔드 |

React의 모든 요청은 이 서버를 거쳐 처리되며, 감정 분석·챗봇 응답 등 AI 관련 요청만 내부적으로 maumPy에 위임합니다.

* **개발 기간**: 2026.03 ~ 2026.10
* **개발 인원**: 1인 (개인 프로젝트)

---

## Tech Stack

### Backend
- **Framework**: Java 17, Spring Boot 4.0, Spring Data JPA, QueryDSL
- **Auth**: JWT (JSON Web Token), Spring Security (OAuth2 Resource Server)
- **DB / Cache**: MariaDB, MongoDB, Redis, Ehcache
- **Build Tool**: Gradle

### AI & Data (별도 저장소, [maumPy](https://github.com/hjhjhj0917/maumPy) 참고)
- **Language & API**: Python, FastAPI, PyTorch
- **Model**: Google Gemini (RAG·요약), KoELECTRA(감정 분석), klue/roberta-base(우울증 분석)

### Frontend & Infrastructure
- **Frontend**: JavaScript, React ([maumReact](https://github.com/hjhjhj0917/maumReact) 참고)
- **Infra/Tools**: GCP(Cloud Storage), Git
- **External API**: Kakao Map API

---

## Key Features

### 1. 유저 인증 및 보안 체계
- **JWT & Redis**: JWT 기반 유저 인증 시스템을 구축하고, Redis로 세션 유효성을 검증합니다.
- **블랙리스트 필터링**: `RedisBlacklistFilter`로 로그아웃/탈퇴 시 토큰을 즉시 무효화합니다.

### 2. AI 기반 일기 분석 (일기 저장 시 자동 실행)
- 일기 저장/수정 시 AI 서버(maumPy)에 감정 분석 + 우울증 예측 + AI 요약 + 감정 기반 음악 추천을 **한 번의 API 호출**로 요청하고, 결과를 `DIARY`/`DIARY_MUSIC` 테이블에 반영합니다.
- 타이핑 중에는 AI 분석 없이 제목/내용만 저장하는 **임시저장**(`draftSave`) 기능을 별도로 제공해 불필요한 분석 호출을 방지합니다.

### 3. 일기 이미지 첨부 (GCS)
- 일기당 최대 3장까지 이미지를 Google Cloud Storage에 업로드하고, 일기 삭제 시 스토리지의 실제 파일도 함께 정리합니다.

### 4. RAG 기반 챗봇 서비스
- MongoDB Atlas Vector Search로 정신건강기관/청년정책 데이터를 유사도 검색하고, Gemini 기반 AI 서버가 생성한 답변을 SSE로 스트리밍 전달합니다.
- 채팅방별 멀티턴 대화 이력을 관리하며, 답변 중 상담기관/정책 카드나 TTS 음성이 함께 오는 경우 이를 구분해 전달합니다.

### 5. 음성 인터페이스 (STT)
- 녹음된 음성 파일을 AI 서버로 프록시해 텍스트로 변환하는 엔드포인트를 제공합니다.

### 6. 자동화된 데이터 관리 (Scheduler)
- `DataUpdateScheduler`, `UserCleanupScheduler`를 통해 공공데이터를 최신 상태로 유지하고 불필요한 유저 데이터를 주기적으로 정리합니다.

---

## Project Structure

```text
src/main/java/com/example/maum/
 ├── auth/            # 인증 관련 권한(UserRole) 및 정보 처리
 ├── config/          # JWT, QueryDSL, Redis, Security 등 환경 설정
 ├── controller/      # ChatBot, Diary(+이미지/음악), Login, Map, Stt, UserInfo 등 API 엔드포인트
 │    ├── exception/  # Global/Auth 예외 처리 핸들러
 │    └── response/   # 공통 응답 형식(CommonResponse) 처리
 ├── dto/             # 데이터 전송 객체 (ChatBot, Diary, DiaryImage, DiaryMusic, UserInfo 등)
 ├── jwt/             # 토큰 리졸버 및 JWT 인증 로직
 ├── repository/      # MariaDB, MongoDB 데이터 접근 인터페이스
 │    └── entity/     # DB 엔티티 및 도큐먼트 객체 (Diary, DiaryImage, DiaryMusic, MentalInst 등)
 ├── scheduler/       # 데이터 및 유저 정보 정리 스케줄러 로직
 ├── security/        # RedisBlacklist 등 보안 필터 로직
 ├── service/         # 도메인별 핵심 비즈니스 로직
 │    └── impl/       # GCS 업로드(GcsService) 등 구현체
 └── util/            # CmmUtil, DateUtil, EncryptUtil 등 공통 유틸리티
```

---

## Getting Started

### 요구 사항
- Java 17 (JDK)
- MariaDB, MongoDB, Redis
- 함께 실행되는 [maumPy](https://github.com/hjhjhj0917/maumPy) AI 서버 (`localhost:8000` 기준)
- GCP 프로젝트 (Cloud Storage — 일기 이미지 업로드용)

### 설정
`src/main/resources/application-secret.yaml`에 DB/Redis/메일/JWT/GCS 등 인증 정보를 구성해야 합니다 (`.gitignore`에 포함되어 저장소에는 올라가지 않습니다). 값은 별도로 안전하게 전달받아 구성해주세요.

### 실행
```bash
./gradlew bootRun
```
서버는 기본적으로 `8080` 포트에서 실행되며, [maumReact](https://github.com/hjhjhj0917/maumReact) 개발 서버(`vite.config.js`의 프록시)가 이 포트를 바라보도록 구성되어 있습니다.
