package com.example.maum.repository.entity;

import com.example.maum.diary.DiaryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diary")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class DiaryEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 외부(API)에 노출하는 식별자. 내부 PK(id)는 밖으로 내보내지 않음
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // DB 가 본문에서 자동 계산하는 컬럼이라 애플리케이션에서는 쓰지 않음
    @Column(name = "char_count", insertable = false, updatable = false)
    private Integer charCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DiaryStatus status;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "weather_condition", length = 20)
    private String weatherCondition;

    @Column(name = "temperature_c", precision = 4, scale = 1)
    private BigDecimal temperatureC;

    @Column(name = "weather_region", length = 50)
    private String weatherRegion;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // DB 트리거(set_updated_at)가 갱신하므로 애플리케이션에서는 쓰지 않음
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public void changeText(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public void changeWeather(String weatherCondition, BigDecimal temperatureC, String weatherRegion) {
        this.weatherCondition = weatherCondition;
        this.temperatureC = temperatureC;
        this.weatherRegion = weatherRegion;
    }

    public void changeFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void changePinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void publish(Instant publishedAt) {
        this.status = DiaryStatus.PUBLISHED;
        this.publishedAt = publishedAt;
    }

    public boolean isPublished() {
        return status == DiaryStatus.PUBLISHED;
    }

    // 분석 대상 텍스트(제목·본문)가 같은지: 수정 시 재분석 여부와 오래된 분석 결과 폐기에 쓴다
    public boolean hasSameText(String title, String content) {
        return this.title.equals(title) && this.content.equals(content);
    }
}
