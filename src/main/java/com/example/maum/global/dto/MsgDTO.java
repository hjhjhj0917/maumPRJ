package com.example.maum.global.dto;

import lombok.Builder;

@Builder
public record MsgDTO(
        int result,
        String msg
) {
}
