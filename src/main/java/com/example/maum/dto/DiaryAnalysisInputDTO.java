package com.example.maum.dto;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.StageStatus;
import lombok.Builder;

import java.util.List;

// 분석 파이프라인이 한 번 읽어 가는 일기 스냅샷. 어느 단계까지 끝났는지(상태)와 이미 끝난 단계의 결과(요약, 상위 감정)를 담아서
// 실패 후 재시도 때 끝난 단계는 다시 하지 않고 이어서 한다.
@Builder
public record DiaryAnalysisInputDTO(
        Long diaryId,
        String diaryPublicId,
        String userPublicId,
        String diaryDate,
        String title,
        String content,
        AnalysisStatus analysisStatus,
        StageStatus summaryStatus,
        StageStatus embeddingStatus,
        String summary,
        List<String> topEmotions
) {
}
