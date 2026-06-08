# MAUM (마음) - 일기 기반 AI 감정 분석 서비스

사용자의 일기 텍스트를 기반으로 감정과 우울증 수치를 분석하고, RAG(검색 증강 생성) 기술을 활용해 개인화된 챗봇 상담을 제공하는 웹 서비스입니다[cite: 3]. 

* **개발 기간**: 2026.03 ~ 2026.06[cite: 3]
* **개발 인원**: 1인 (개인 프로젝트)[cite: 3]

## Tech Stack

### Backend
- **Framework**: Java, Spring Boot, JPA[cite: 3]
- **Auth**: JWT (JSON Web Token), Spring Security[cite: 3]
- **DB / Cache**: MariaDB, MongoDB, Redis[cite: 3]
- **Build Tool**: Gradle[cite: 2]

### AI & Data
- **Language & API**: Python, Flask, PyTorch[cite: 3]
- **Model**: HyperCLOVA X (임베딩V2, HCX-007, RAG Reasoning), KoELECTRA, klue/bert-base[cite: 3]

### Frontend & Infrastructure
- **Frontend**: JavaScript, React[cite: 3]
- **Infra/Tools**: GCP, Docker, Git[cite: 3]
- **External API**: Kakao Map API[cite: 3]

---

## Key Features

### 1. 유저 인증 및 보안 체계
- **JWT & Redis**: JWT 기반의 유저 인증 시스템을 구축하고, Redis를 활용하여 세션 유효성을 검증합니다[cite: 3].
- **블랙리스트 필터링**: `RedisBlacklistFilter`를 구현하여 로그아웃 및 보안 처리 시 안전한 토큰 관리를 지원합니다[cite: 2].

### 2. AI 기반 감정 및 우울증 분석
- **KoELECTRA 감정 분석**: 한국어 감정 분류 데이터셋(KOTE)으로 파인튜닝된 KoELECTRA 모델을 적용하여 작성된 일기 텍스트의 감정을 정밀하게 분류합니다[cite: 3].
- **우울증 예측 모델**: AI Hub에서 제공하는 심리상담 데이터를 활용, `klue/bert-base` 모델을 파인튜닝하여 우울증 모델을 구현했습니다[cite: 3].

### 3. HyperCLOVA X & RAG 기반 챗봇 서비스
- **데이터 벡터화**: 공공 API 데이터를 HyperCLOVA X 임베딩 V2 모델로 벡터화한 후 MongoDB에 저장합니다[cite: 3].
- **유사도 검색**: MongoDB Atlas Vector Search 기능을 적용하여 유사도 기반의 빠르고 정확한 데이터 검색 시스템을 구현했습니다[cite: 3].
- **스마트 챗봇**: HyperCLOVA X (HCX-007) 추론 모델로 일기 내용을 요약하고, RAG Reasoning 모델의 Tool Call 기능을 활용하여 사용자와 상호작용하는 챗봇을 제공합니다[cite: 3].

### 4. 자동화된 데이터 관리 (Scheduler)
- `DataUpdateScheduler` 및 `UserCleanupScheduler`를 통해 시스템의 데이터를 최신 상태로 유지하고 불필요한 유저 데이터를 주기적으로 정리합니다[cite: 2].

---

## Project Structure

```text
src/main/java/com/example/maum/
 ├── auth/            # 인증 관련 권한(UserRole) 및 정보 처리[cite: 2]
 ├── config/          # JWT, QueryDSL, Redis, Security 등 환경 설정[cite: 2]
 ├── controller/      # ChatBot, Diary, Login, Map, UserInfo 등 API 엔드포인트[cite: 2]
 │    ├── exception/  # Global/Auth 예외 처리 핸들러[cite: 2]
 │    └── response/   # 공통 응답 형식(CommonResponse) 처리[cite: 2]
 ├── dto/             # 데이터 전송 객체 (ChatBot, Diary, UserInfo 등)[cite: 2]
 ├── jwt/             # 토큰 리졸버 및 JWT 인증 로직[cite: 2]
 ├── repository/      # MariaDB, MongoDB 데이터 접근 인터페이스 (Diary, MentalInst 등)[cite: 2]
 │    └── entity/     # DB 엔티티 및 도큐먼트 객체 설계[cite: 2]
 ├── scheduler/       # 데이터 및 유저 정보 정리 스케줄러 로직[cite: 2]
 ├── security/        # RedisBlacklist 등 보안 필터 로직[cite: 2]
 ├── service/         # 도메인별 핵심 비즈니스 로직(Impl) 구현부[cite: 2]
 └── util/            # CmmUtil, DateUtil, EncryptUtil 등 공통 유틸리티[cite: 2]
