package com.example.maum.diary;

// diary_analysis.analysis_status 의 CHECK 제약과 같은 값이어야 함 (로컬 모델 분석: 감정 점수 44개 + 우울 지수)
public enum AnalysisStatus {

    PENDING,
    DONE,
    // 1차 감성 분류에서 전 문장이 뚜렷한 긍정이라 우울 모델을 생략한 경우 (dep_score 는 NULL, dep_level 은 0)
    DEP_SKIPPED,
    FAILED;

    // 감정 점수가 저장된 상태인지 (우울 모델을 생략했어도 감정 점수는 저장됨)
    public boolean isFinished() {
        return this == DONE || this == DEP_SKIPPED;
    }
}
