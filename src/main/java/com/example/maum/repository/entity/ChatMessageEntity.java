package com.example.maum.repository.entity;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.chat.ChatRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_message")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class ChatMessageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private Long sessionId;

    @Column(name = "seq", nullable = false, updatable = false)
    private int seq;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, updatable = false, length = 10)
    private ChatRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ChatMessageStatus status;

    // 재전송 멱등성 키. 사용자 메시지와 그 답변이 같은 값을 가짐
    @Column(name = "client_msg_id", updatable = false)
    private UUID clientMsgId;

    @Column(name = "has_audio", nullable = false)
    private boolean hasAudio;

    // 정책/상담기관 카드 복원용
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cards", columnDefinition = "jsonb")
    private List<Map<String, Object>> cards;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // 스트림이 끝난 뒤 최종 내용과 상태를 한 번에 기록
    public void finish(ChatMessageStatus status, String content, boolean hasAudio, List<Map<String, Object>> cards) {
        this.status = status;
        this.content = content;
        this.hasAudio = hasAudio;
        this.cards = cards;
    }
}
