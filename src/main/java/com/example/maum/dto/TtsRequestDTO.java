package com.example.maum.dto;

import lombok.Builder;

@Builder
public record TtsRequestDTO(
        String text
) {
}
