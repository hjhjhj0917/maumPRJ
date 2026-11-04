package com.example.maum.dto;

import lombok.Builder;

// 세부 감정 하나의 점수. score 는 모델의 원 확률(0~1)이며, 화면에 보일 비율 계산은 프론트에서 정한다
@Builder
public record EmotionScoreDTO(
        String name,
        String groupCode,
        String color,
        float score
) {
}
