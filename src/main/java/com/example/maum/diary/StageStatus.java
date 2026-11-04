package com.example.maum.diary;

// diary_analysis.summary_status / embedding_status 의 CHECK 제약과 같은 값이어야 함 (외부 AI 호출 단계의 진행 상태)
public enum StageStatus {

    PENDING,
    DONE,
    FAILED,
    // 프라이버시 모드로 외부 AI 호출을 건너뜀 (생성 중과 구분하려는 값, 재시도 대상 아님)
    SKIPPED
}
