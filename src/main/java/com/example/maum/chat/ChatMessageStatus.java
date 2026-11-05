package com.example.maum.chat;

// STREAMING: 답변 생성 중, INTERRUPTED: 연결이 끊겨 일부만 저장됨, FAILED: AI 서버 오류
public enum ChatMessageStatus {
    STREAMING, COMPLETE, INTERRUPTED, FAILED
}
