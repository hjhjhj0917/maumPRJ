package com.example.maum.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DiaryDTO(
        Integer diaryNo,
        Integer userNo,
        String title,
        String content,
        String summary,
        String mainEmotion,
        String emotionColor,
        Integer depLvl,
        Double depScore,
        Integer symptomYn,
        String createdAt
) {
}
