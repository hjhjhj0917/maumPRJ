package com.example.maum.dto;

import lombok.Builder;

@Builder
public record EmotionStatDTO(
        String emotion,
        int count,
        String color
) {}