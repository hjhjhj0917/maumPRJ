package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DiaryDTO(
        Integer diaryNo,
        String userNo,
        String title,
        String content,
        String summary,
        String mainEmotion,
        String emotionColor,
        Integer depLvl,
        BigDecimal depScore,
        Integer symptomYn,
        String createdAt,
        Integer isFavorite,
        Integer isPinned,
        List<DiaryImageDTO> images,
        List<DiaryMusicDTO> musics
) {
}
