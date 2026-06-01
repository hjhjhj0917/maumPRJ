package com.example.maum.dto;

import lombok.Builder;

@Builder
public record MailDTO(
        String title,
        String content,
        String receiver
) {
}
