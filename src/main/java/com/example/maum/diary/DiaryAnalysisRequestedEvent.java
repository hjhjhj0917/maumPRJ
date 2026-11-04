package com.example.maum.diary;

// 일기가 발행되거나 본문이 바뀌어 (재)분석이 필요할 때, 트랜잭션이 커밋된 뒤에 분석을 시작시키는 신호
public record DiaryAnalysisRequestedEvent(Long diaryId) {
}
