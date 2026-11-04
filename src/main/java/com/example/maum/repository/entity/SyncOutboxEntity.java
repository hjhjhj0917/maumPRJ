package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.Instant;

// 외부 저장소(Mongo 벡터 등) 삭제를 DB 변경과 같은 트랜잭션에 기록해 두는 작업 목록. 처리하는 워커는 4단계에서 붙인다
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

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
