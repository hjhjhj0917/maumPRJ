package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;

// userNo 는 AI 서버(FastAPI)가 아직 쓰는 필드명이라 유지하되 값은 사용자 publicId 임
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChatBotDTO(
        String userNo,
        String message,
        String sessionPublicId,
        String clientMsgId,
        List<ChatMessageDTO> history
) {
}
