package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChatBotDTO(
        String userNo,
        String message,
        List<ChatMessageDTO> history
) {
}
