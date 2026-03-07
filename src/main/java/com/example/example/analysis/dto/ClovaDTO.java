package com.example.example.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClovaDTO {
    private List<Message> messages;
    private double temperature = 0.3;
    private int maxTokens = 1000;
    private boolean includeThinking = true; // HCX-007의 핵심 기능 활성화

    @AllArgsConstructor
    @Getter
    public static class Message {
        private String role;
        private String content;
    }
}
