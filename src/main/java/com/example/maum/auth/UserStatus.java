package com.example.maum.auth;

// users.status 의 CHECK 제약과 같은 값이어야 함
public enum UserStatus {

    PENDING_EMAIL_VERIFY,
    PENDING_ONBOARDING,
    ACTIVE,
    SUSPENDED,
    WITHDRAWN_PENDING
}
