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
    CONSTRAINT ck_consent_doc_type CHECK (consent_type IN ('TERMS', 'PRIVACY', 'SENSITIVE_HEALTH')),
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

-- ---------------------------------------------------------------------------
-- 일기
-- ---------------------------------------------------------------------------
CREATE TABLE emotion (
    code       VARCHAR(20) PRIMARY KEY,
    name_ko    VARCHAR(20) NOT NULL,
    color      CHAR(7)     NOT NULL,
    sort_order SMALLINT    NOT NULL,
    CONSTRAINT ck_emotion_color CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT uq_emotion_name UNIQUE (name_ko)
);

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
    published_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_diary_public_id UNIQUE (public_id),
    CONSTRAINT ck_diary_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT ck_diary_published CHECK (
        status = 'DRAFT'
        OR (published_at IS NOT NULL AND btrim(title) <> '' AND btrim(content) <> ''))
);
-- 달력/목록/통계: 확정본만 인덱싱 (조회는 반드시 status = 'PUBLISHED' 조건 포함)
CREATE INDEX ix_diary_user_date ON diary (user_id, diary_date, id) WHERE status = 'PUBLISHED';
-- 임시 저장 목록
CREATE INDEX ix_diary_user_draft ON diary (user_id, updated_at DESC) WHERE status = 'DRAFT';
CREATE TRIGGER trg_diary_updated BEFORE UPDATE ON diary
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 분석 결과(1:1). 분석 행이 없으면 아직 분석 전/실패.
-- dep_score 척도는 v1 확인 필요(0~1 확률로 가정). dep_level 범위는 모델 기준 확정 후 CHECK 추가.
CREATE TABLE diary_analysis (
    diary_id           BIGINT PRIMARY KEY REFERENCES diary (id) ON DELETE CASCADE,
    summary            TEXT,
    main_emotion_code  VARCHAR(20) REFERENCES emotion (code),
    dep_level          SMALLINT,
    dep_score          NUMERIC(5, 4),
    symptom_flag       BOOLEAN     NOT NULL DEFAULT FALSE,
    model_version      VARCHAR(50),
    analyzed_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    embedding_status   VARCHAR(10) NOT NULL DEFAULT 'PENDING',   -- Mongo Atlas 벡터 동기화 상태
    embedding_version  VARCHAR(50),
    embedded_at        TIMESTAMPTZ,
    CONSTRAINT ck_analysis_dep_level CHECK (dep_level IS NULL OR dep_level >= 0),
    CONSTRAINT ck_analysis_dep_score CHECK (dep_score IS NULL OR (dep_score >= 0 AND dep_score <= 1)),
    CONSTRAINT ck_analysis_embedding CHECK (embedding_status IN ('PENDING', 'DONE', 'FAILED'))
);
CREATE INDEX ix_analysis_emotion ON diary_analysis (main_emotion_code);
CREATE INDEX ix_analysis_embedding_retry ON diary_analysis (analyzed_at) WHERE embedding_status <> 'DONE';

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
    cards         JSONB,                                    -- [[CARD]] 복원용
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
