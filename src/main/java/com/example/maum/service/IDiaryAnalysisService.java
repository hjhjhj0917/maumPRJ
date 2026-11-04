package com.example.maum.service;

public interface IDiaryAnalysisService {

    // 일기 1건의 분석을 끝난 단계는 건너뛰고 이어서 실행함 (발행 직후와 재시도 모두 같은 진입점)
    void analyze(Long diaryId);
}
