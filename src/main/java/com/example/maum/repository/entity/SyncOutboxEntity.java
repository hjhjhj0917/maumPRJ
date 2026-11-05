package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.Instant;

// 외부 저장소(Mongo 벡터, 스토리지 파일) 삭제를 DB 변경과 같은 트랜잭션에 기록해 두는 작업 목록. 처리는 SyncOutboxWorker 가 함
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sync_outbox")
@DynamicInsert
@Builder
@Entity
public class SyncOutboxEntity implements Serializable {

    public static final String TARGET_MONGO = "MONGO";
    public static final String TARGET_STORAGE = "STORAGE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_PURGE_USER = "PURGE_USER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "target", nullable = false, length = 10)
    private String target;

    @Column(name = "action", nullable = false, length = 10)
    private String action;

    @Column(name = "ref_key", nullable = false, length = 500)
    private String refKey;

    // 실패할 때마다 늘어남. DB 기본값(0)을 쓰도록 새 행 저장 시에는 값을 보내지 않음
    @Column(name = "attempts")
    private Short attempts;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public void markFailed() {
        this.attempts = (short) ((attempts == null ? 0 : attempts) + 1);
    }
}
