package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_session")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class ChatSessionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    // 롤링 요약과 반영된 마지막 seq. 요약 생성은 AI 서버 엔드포인트가 생기는 6단계에서 붙임
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "summarized_until_seq", nullable = false)
    private int summarizedUntilSeq;

    @Column(name = "last_seq", nullable = false)
    private int lastSeq;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // DB 트리거(set_updated_at)가 갱신하므로 애플리케이션에서는 쓰지 않음
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // 행 락을 잡은 상태에서만 호출할 것(동시 요청이 같은 seq 를 받지 않도록)
    public int nextSeq() {
        this.lastSeq += 1;
        return this.lastSeq;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changePinned(boolean pinned) {
        this.pinned = pinned;
    }
}
