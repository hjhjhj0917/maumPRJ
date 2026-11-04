package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diary_image")
@DynamicInsert
@Builder
@Entity
public class DiaryImageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder;

    // 파일 위치(버킷 안의 경로)만 저장하고, 화면에 내보낼 때 URL 로 바꾼다
    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
