package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.List;
import java.util.Map;

// role 은 "user"/"bot" (AI 서버와 화면이 쓰는 값)
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChatMessageDTO(
        Long chatMsgNo,
        String role,
        String content,
        Boolean hasAudio,
        String status,
        List<Map<String, Object>> cards
) {
}
