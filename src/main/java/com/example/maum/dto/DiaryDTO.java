package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

// 일기 요청·응답 공용. 내부 PK 대신 publicId(UUID 문자열)만 밖으로 내보낸다.
// 분석 결과 필드(summary~musics)는 응답에서만 채워지며, 단계별 상태(analysisStatus, summaryStatus)로 "생성 중"을 구분한다.
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DiaryDTO(
        String publicId,
        String userPublicId,
        String title,
        String content,
        String diaryDate,
        String status,
        Boolean isFavorite,
        Boolean isPinned,
        String weatherCondition,
        BigDecimal temperatureC,
        String weatherRegion,
        String analysisStatus,
        String summaryStatus,
        String summary,
        String mainEmotion,
        String emotionColor,
        List<EmotionScoreDTO> topEmotions,
        Integer depLevel,
        BigDecimal depScore,
        List<DiaryImageDTO> images,
        List<DiaryMusicDTO> musics
) {
}
