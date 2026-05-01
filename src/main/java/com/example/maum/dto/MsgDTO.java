package com.example.maum.dto;

import lombok.Builder;

@Builder
public record MsgDTO(
        int result,
        String msg
) {
}
