package com.example.maum.dto;

import java.util.List;

public record TtsResponseDTO(
        List<String> audioChunks
) {
}
