-- MauM v2 baseline (PostgreSQL 15+)
-- 원칙: 내부 PK는 BIGINT IDENTITY, 외부 노출 테이블(users/diary/chat_session)만 public_id(UUID) 추가.
--       시각은 timestamptz(UTC 저장), 일기 날짜는 DATE. 상태값은 VARCHAR + CHECK(변경 시 Flyway로 수정 용이).
--       이메일은 앱에서 소문자·trim 정규화 후 저장(CHECK로 강제). 확장 모듈 불필요(gen_random_uuid는 내장).

-- ---------------------------------------------------------------------------
-- 공통: updated_at 자동 갱신
-- ---------------------------------------------------------------------------
CREATE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- 회원
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    email             VARCHAR(254) NOT NULL,
    email_verified_at TIMESTAMPTZ,
    nickname          VARCHAR(30),
    birth_year        SMALLINT,
    profile_image_key VARCHAR(500),                       -- NULL이면 기본 이미지
    role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status            VARCHAR(30)  NOT NULL,
    withdrawn_at      TIMESTAMPTZ,
    purge_at          TIMESTAMPTZ,                        -- 영구 삭제 예정 시각(탈퇴 유예 만료)
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_public_id UNIQUE (public_id),
    CONSTRAINT ck_users_email_lower CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_users_status CHECK (status IN
        ('PENDING_EMAIL_VERIFY', 'PENDING_ONBOARDING', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN_PENDING')),
    CONSTRAINT ck_users_birth_year CHECK (birth_year IS NULL OR birth_year >= 1900),
    -- 온보딩 전(구글 신규)만 닉네임·출생연도가 비어 있을 수 있다.
    CONSTRAINT ck_users_profile_required CHECK (
        status = 'PENDING_ONBOARDING' OR (nickname IS NOT NULL AND birth_year IS NOT NULL)),
    -- 탈퇴 유예 상태일 때만 withdrawn_at/purge_at 존재.
    CONSTRAINT ck_users_withdrawal CHECK (
        (status = 'WITHDRAWN_PENDING') = (withdrawn_at IS NOT NULL AND purge_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_users_email ON users (email);
CREATE INDEX ix_users_purge_due ON users (purge_at) WHERE status = 'WITHDRAWN_PENDING';
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- LOCAL: provider_user_id = users.public_id 문자열, password_hash(BCrypt) 필수.
-- GOOGLE 등: provider_user_id = 제공자의 고유 ID(sub), password_hash 없음.
CREATE TABLE user_auth_provider (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    provider         VARCHAR(20)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(100),
    last_login_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_auth_provider CHECK (provider IN ('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER')),
    CONSTRAINT ck_auth_password CHECK ((provider = 'LOCAL') = (password_hash IS NOT NULL)),
    CONSTRAINT uq_auth_provider_identity UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_auth_user_provider UNIQUE (user_id, provider)
);

-- 인증 메일 토큰은 해시(SHA-256 hex)만 저장.
CREATE TABLE email_verification (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    purpose       VARCHAR(20) NOT NULL,
    token_hash    CHAR(64)    NOT NULL,
    expires_at    TIMESTAMPTZ NOT NULL,
    verified_at   TIMESTAMPTZ,
    attempt_count SMALLINT    NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_email_verif_purpose CHECK (purpose IN ('SIGNUP', 'PASSWORD_RESET', 'ACCOUNT_RECOVERY')),
    CONSTRAINT uq_email_verif_token UNIQUE (token_hash)
);
CREATE INDEX ix_email_verif_user ON email_verification (user_id, purpose);
CREATE INDEX ix_email_verif_expires ON email_verification (expires_at);

-- 약관 문서(버전 관리) + 사용자별 동의 이력. 마케팅 수신 동의는 받지 않는다.
CREATE TABLE consent_document (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    consent_type VARCHAR(30) NOT NULL,
    version      VARCHAR(20) NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    content_hash CHAR(64)    NOT NULL,
    -- LOCATION(위치정보 이용)은 선택 동의: 날씨 기능에만 쓰이며 가입 필수 항목이 아니다.
    CONSTRAINT ck_consent_doc_type CHECK (consent_type IN ('TERMS', 'PRIVACY', 'SENSITIVE_HEALTH', 'LOCATION')),
    CONSTRAINT uq_consent_doc UNIQUE (consent_type, version)
);
CREATE TABLE user_consent (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    document_id  BIGINT      NOT NULL REFERENCES consent_document (id),
    agreed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    withdrawn_at TIMESTAMPTZ,
    CONSTRAINT uq_user_consent UNIQUE (user_id, document_id)
);

-- 일기 작성 리마인드 설정(계정 보안 알림은 끌 수 없어 설정 컬럼이 없다). 가입 시 행을 만들거나 최초 조회 시 생성한다.
-- reminder_days: 비트마스크(월=1, 화=2, 수=4, 목=8, 금=16, 토=32, 일=64). 시각은 KST 기준 로컬 시간.
CREATE TABLE user_notification_setting (
    user_id          BIGINT PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    reminder_enabled BOOLEAN     NOT NULL DEFAULT FALSE,
    reminder_days    SMALLINT    NOT NULL DEFAULT 127,
    reminder_time    TIME        NOT NULL DEFAULT '21:00',
    last_reminded_at TIMESTAMPTZ,                               -- 같은 날 중복 발송 방지
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_notification_days CHECK (reminder_days BETWEEN 0 AND 127)
);
CREATE INDEX ix_notification_due ON user_notification_setting (reminder_time) WHERE reminder_enabled;
CREATE TRIGGER trg_notification_updated BEFORE UPDATE ON user_notification_setting
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 리프레시 토큰: 해시만 저장, 사용 시 회전(replaced_by), 재사용 감지 시 family 전체 폐기.
CREATE TABLE refresh_token (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    family_id      UUID        NOT NULL,
    token_hash     CHAR(64)    NOT NULL,
    expires_at     TIMESTAMPTZ NOT NULL,
    revoked_at     TIMESTAMPTZ,
    replaced_by_id BIGINT REFERENCES refresh_token (id) ON DELETE SET NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);
CREATE INDEX ix_refresh_user ON refresh_token (user_id);
CREATE INDEX ix_refresh_family ON refresh_token (family_id);
CREATE INDEX ix_refresh_expires ON refresh_token (expires_at);
-- 자기참조 FK(ON DELETE SET NULL): 만료 토큰 삭제 때 이 인덱스가 없으면 행마다 테이블을 전체 스캔한다.
CREATE INDEX ix_refresh_replaced_by ON refresh_token (replaced_by_id) WHERE replaced_by_id IS NOT NULL;

-- ---------------------------------------------------------------------------
-- 일기
-- ---------------------------------------------------------------------------
-- 감정은 2단계: 대분류 9종(emotion_group, 색상 보유) > KOTE 세부 감정 44종(emotion).
-- 색상은 대분류에만 둔다. 세부 감정 이름(name_ko)은 모델 출력 라벨과 동일해 Spring이 name_ko -> code로 변환한다.
CREATE TABLE emotion_group (
    code       VARCHAR(20) PRIMARY KEY,
    name_ko    VARCHAR(20) NOT NULL,
    color      CHAR(7)     NOT NULL,
    sort_order SMALLINT    NOT NULL,
    CONSTRAINT ck_emotion_group_color CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT uq_emotion_group_name UNIQUE (name_ko)
);
-- id: 점수 테이블(diary_emotion_score) 키로 쓰는 SMALLINT. 저장된 점수가 참조하므로 값이 바뀌면 안 된다(V2 시드에서 고정).
-- code: 코드/SQL에서 읽기 쉬운 식별자, name_ko: 모델 출력 라벨.
CREATE TABLE emotion (
    id         SMALLINT    PRIMARY KEY,
    code       VARCHAR(30) NOT NULL,
    name_ko    VARCHAR(30) NOT NULL,
    group_code VARCHAR(20) NOT NULL REFERENCES emotion_group (code),
    sort_order SMALLINT    NOT NULL,
    CONSTRAINT uq_emotion_code UNIQUE (code),
    CONSTRAINT uq_emotion_name UNIQUE (name_ko)
);
CREATE INDEX ix_emotion_group ON emotion (group_code);

-- 하루 1개 제한 없음(임시 저장). 분석·음악·임베딩은 PUBLISHED 가 될 때만 생성한다.
CREATE TABLE diary (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id    UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    diary_date   DATE         NOT NULL,
    title        VARCHAR(200) NOT NULL DEFAULT '',
    content      TEXT         NOT NULL DEFAULT '',
    status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    is_favorite  BOOLEAN      NOT NULL DEFAULT FALSE,
    is_pinned    BOOLEAN      NOT NULL DEFAULT FALSE,
    -- 작성 시점 날씨(브라우저 좌표로 날씨 API 조회, 좌표 자체는 저장하지 않음). 조회 실패 시 NULL.
    weather_condition VARCHAR(20),
    temperature_c     NUMERIC(4, 1),
    weather_region    VARCHAR(50),                         -- 시/구 단위 대략적 지역명
    published_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_diary_public_id UNIQUE (public_id),
    CONSTRAINT ck_diary_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    -- 날씨 API 응답을 이 분류로 매핑한다(제공자 확정 후 값 조정 가능).
    CONSTRAINT ck_diary_weather CHECK (weather_condition IS NULL OR weather_condition IN
        ('CLEAR', 'PARTLY_CLOUDY', 'CLOUDY', 'RAIN', 'SNOW', 'SLEET', 'FOG', 'THUNDERSTORM', 'OTHER')),
    CONSTRAINT ck_diary_published CHECK (
        status = 'DRAFT'
        OR (published_at IS NOT NULL AND btrim(title) <> '' AND btrim(content) <> ''))
);
-- user_id FK 전용(전체 행). 아래 두 인덱스는 부분 인덱스라 탈퇴 purge의 DELETE ... WHERE user_id 에는 쓰이지 않는다.
CREATE INDEX ix_diary_user ON diary (user_id);
-- 달력/목록/통계: 확정본만 인덱싱 (조회는 반드시 status = 'PUBLISHED' 조건 포함)
CREATE INDEX ix_diary_user_date ON diary (user_id, diary_date, id) WHERE status = 'PUBLISHED';
-- 임시 저장 목록
CREATE INDEX ix_diary_user_draft ON diary (user_id, updated_at DESC) WHERE status = 'DRAFT';
CREATE TRIGGER trg_diary_updated BEFORE UPDATE ON diary
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 분석 결과(1:1). 발행(PUBLISHED) 시 PENDING 행을 만들고 분석이 끝나면 상태를 갱신한다.
-- analysis_status: PENDING(대기) / DONE(정상 완료) / DEP_SKIPPED(우울 모델 생략) / FAILED(실패, 재시도 대상)
--   DEP_SKIPPED: 1차 감성 분류에서 전 문장이 뚜렷한 긍정이라 우울 모델을 돌리지 않은 경우. 이때 dep_score 는 실제 확률이 아니므로 NULL로 두고(CHECK),
--   dep_level 은 0 이다. 우울 추이 통계의 평균(dep_score)은 NULL 이 자동 제외된다.
-- dep_level: v1 우울 모델은 이진 분류라 0(정상) / 1(우울 증상 의심). 증상 여부는 dep_level = 1 로 판단한다(별도 플래그 없음).
-- dep_score: 청크별 우울 확률의 평균(0~1). 대표 감정(main_group_code)은 분석 완료 후 채운다.
CREATE TABLE diary_analysis (
    diary_id           BIGINT PRIMARY KEY REFERENCES diary (id) ON DELETE CASCADE,
    analysis_status    VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    summary            TEXT,
    main_group_code    VARCHAR(20) REFERENCES emotion_group (code),   -- 대표 감정(대분류), 색상 표시용
    dep_level          SMALLINT,
    dep_score          NUMERIC(5, 4),
    model_version      VARCHAR(50),
    analyzed_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    embedding_status   VARCHAR(10) NOT NULL DEFAULT 'PENDING',   -- Mongo Atlas 벡터 동기화 상태
    embedding_version  VARCHAR(50),
    embedded_at        TIMESTAMPTZ,
    CONSTRAINT ck_analysis_status CHECK (analysis_status IN ('PENDING', 'DONE', 'DEP_SKIPPED', 'FAILED')),
    CONSTRAINT ck_analysis_dep_level CHECK (dep_level IS NULL OR dep_level IN (0, 1)),
    CONSTRAINT ck_analysis_dep_score CHECK (dep_score IS NULL OR (dep_score >= 0 AND dep_score <= 1)),
    CONSTRAINT ck_analysis_dep_skipped CHECK (analysis_status <> 'DEP_SKIPPED' OR (dep_score IS NULL AND dep_level = 0)),
    CONSTRAINT ck_analysis_embedding CHECK (embedding_status IN ('PENDING', 'DONE', 'FAILED'))
);
CREATE INDEX ix_analysis_group ON diary_analysis (main_group_code);
-- 분석 대기/실패 건 재처리 대상 조회
CREATE INDEX ix_analysis_retry ON diary_analysis (analyzed_at) WHERE analysis_status IN ('PENDING', 'FAILED');
CREATE INDEX ix_analysis_embedding_retry ON diary_analysis (analyzed_at) WHERE embedding_status <> 'DONE';

-- 일기별 세부 감정 점수(44개 전부 저장). KOTE는 감정별 독립 확률(sigmoid)이라 합이 1이 아니다.
-- 화면의 "상위 10개 %"는 점수 내림차순 10개를 조회해 만들고, 표시 비율 계산(원 확률 vs 합 100% 정규화)은 앱에서 결정한다.
-- 가장 큰 테이블이라 키를 SMALLINT로 줄이고, PK에 score를 포함(INCLUDE)해 통계가 인덱스만으로 끝나게 한다.
-- 감정 기준 역방향 조회는 쓰는 쿼리가 없어 별도 인덱스를 두지 않는다.
CREATE TABLE diary_emotion_score (
    diary_id   BIGINT   NOT NULL REFERENCES diary (id) ON DELETE CASCADE,
    emotion_id SMALLINT NOT NULL REFERENCES emotion (id),
    score      REAL     NOT NULL,
    PRIMARY KEY (diary_id, emotion_id) INCLUDE (score),
    CONSTRAINT ck_emotion_score CHECK (score >= 0 AND score <= 1)
);

CREATE TABLE diary_music (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    diary_id        BIGINT       NOT NULL REFERENCES diary (id) ON DELETE CASCADE,
    track_order     SMALLINT     NOT NULL,
    track_id        VARCHAR(64)  NOT NULL,
    track_name      VARCHAR(200) NOT NULL,
    artist_name     VARCHAR(200) NOT NULL,
    album_image_url VARCHAR(500),
    spotify_url     VARCHAR(300) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_diary_music_order UNIQUE (diary_id, track_order)
);

CREATE TABLE diary_image (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    diary_id    BIGINT       NOT NULL REFERENCES diary (id) ON DELETE CASCADE,
    sort_order  SMALLINT     NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_diary_image_order UNIQUE (diary_id, sort_order)
);

-- ---------------------------------------------------------------------------
-- 챗봇
-- ---------------------------------------------------------------------------
CREATE TABLE chat_session (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id            UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id              BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    title                VARCHAR(100),
    is_pinned            BOOLEAN     NOT NULL DEFAULT FALSE,
    summary              TEXT,                              -- 롤링 요약
    summarized_until_seq INT         NOT NULL DEFAULT 0,    -- 요약에 반영된 마지막 seq
    last_seq             INT         NOT NULL DEFAULT 0,    -- 다음 seq 채번용 (행 락으로 +1)
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_chat_session_public_id UNIQUE (public_id),
    CONSTRAINT ck_chat_session_seq CHECK (summarized_until_seq <= last_seq)
);
CREATE INDEX ix_chat_session_user ON chat_session (user_id, is_pinned DESC, updated_at DESC);
CREATE TRIGGER trg_chat_session_updated BEFORE UPDATE ON chat_session
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE chat_message (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id    BIGINT      NOT NULL REFERENCES chat_session (id) ON DELETE CASCADE,
    seq           INT         NOT NULL,
    role          VARCHAR(10) NOT NULL,
    content       TEXT        NOT NULL,
    status        VARCHAR(15) NOT NULL DEFAULT 'COMPLETE',
    token_count   INT,
    model_version VARCHAR(50),
    client_msg_id UUID,                                     -- 재전송 멱등성
    has_audio     BOOLEAN     NOT NULL DEFAULT FALSE,
    cards         JSONB,                                    -- 카드 복원용 + 상담기관 지도 카드(기관명·주소·좌표·연락처 등)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_chat_message_role CHECK (role IN ('user', 'bot')),
    CONSTRAINT ck_chat_message_status CHECK (status IN ('STREAMING', 'COMPLETE', 'INTERRUPTED', 'FAILED')),
    CONSTRAINT uq_chat_message_seq UNIQUE (session_id, seq),
    CONSTRAINT uq_chat_message_client UNIQUE (session_id, client_msg_id, role)
);
-- 같은 세션에서 동시에 스트리밍 중인 응답은 하나만 허용
CREATE UNIQUE INDEX uq_chat_one_streaming ON chat_message (session_id) WHERE status = 'STREAMING';

CREATE TABLE chat_message_source (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message_id  BIGINT      NOT NULL REFERENCES chat_message (id) ON DELETE CASCADE,
    source_type VARCHAR(10) NOT NULL,
    source_ref  VARCHAR(64),                                -- 일기 public_id 등 (원문 복제 금지)
    score       REAL,
    query_text  VARCHAR(500),
    CONSTRAINT ck_chat_source_type CHECK (source_type IN ('DIARY', 'WELFARE', 'HOSPITAL'))
);
CREATE INDEX ix_chat_source_message ON chat_message_source (message_id);

-- ---------------------------------------------------------------------------
-- 외부 저장소 동기화 작업 목록(Outbox)
-- ---------------------------------------------------------------------------
-- 일기 삭제·탈퇴 purge 때 Mongo 벡터/스토리지 파일 삭제를 같은 트랜잭션에서 여기에 기록하고 워커가 재시도한다.
-- diary 행이 CASCADE 로 사라져도 삭제할 대상을 잃지 않도록 FK 없이 키만 보관한다.
-- ref_key: DIARY 벡터는 diary.public_id, 스토리지는 storage_key, 탈퇴 일괄 삭제는 users.public_id.
-- 처리 완료 시 행을 삭제한다. 실패가 반복되는 행(attempts)은 운영에서 확인한다.
CREATE TABLE sync_outbox (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    target     VARCHAR(10)  NOT NULL,
    action     VARCHAR(10)  NOT NULL,
    ref_key    VARCHAR(500) NOT NULL,
    attempts   SMALLINT     NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_outbox_target CHECK (target IN ('MONGO', 'STORAGE')),
    CONSTRAINT ck_outbox_action CHECK (action IN ('UPSERT', 'DELETE', 'PURGE_USER'))
);
