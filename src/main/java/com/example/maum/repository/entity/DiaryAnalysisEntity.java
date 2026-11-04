package com.example.maum.repository.entity;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.StageStatus;
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

// 일기 1건당 분석 결과 1행. 단계별(감정·우울 → 요약·음악 → 임베딩) 상태를 따로 가져서 한 단계가 실패해도 앞 단계 결과는 남는다.
// analyzedAt 은 마지막으로 상태가 바뀐 시각이며, 재처리 대상 판단(너무 오래됐거나 방금 시작한 건 제외)에 쓴다.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diary_analysis")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class DiaryAnalysisEntity implements Serializable {

    @Id
    @Column(name = "diary_id")
    private Long diaryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 12)
    private AnalysisStatus analysisStatus;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_status", nullable = false, length = 10)
    private StageStatus summaryStatus;

    // 대표 감정(대분류). 필터·달력 색상 전용이며 화면의 대표 감정은 점수 1위로 따로 구한다
    @Column(name = "main_group_code", length = 20)
    private String mainGroupCode;

    @Column(name = "dep_level")
    private Integer depLevel;

    @Column(name = "dep_score", precision = 5, scale = 4)
    private BigDecimal depScore;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "embedding_status", nullable = false, length = 10)
    private StageStatus embeddingStatus;

    @Column(name = "embedding_version", length = 50)
    private String embeddingVersion;

    @Column(name = "embedded_at")
    private Instant embeddedAt;

    // 발행 직후의 초기 상태. 프라이버시 모드면 외부 AI 단계는 처음부터 SKIPPED
    public static DiaryAnalysisEntity pending(Long diaryId, boolean aiEnabled) {
        StageStatus aiStage = aiEnabled ? StageStatus.PENDING : StageStatus.SKIPPED;

        return DiaryAnalysisEntity.builder()
                .diaryId(diaryId)
                .analysisStatus(AnalysisStatus.PENDING)
                .summaryStatus(aiStage)
                .embeddingStatus(aiStage)
                .analyzedAt(Instant.now())
                .build();
    }

    // 본문이 바뀌어 처음부터 다시 분석할 때: 이전 결과를 모두 비운다
    public void reset(boolean aiEnabled) {
        StageStatus aiStage = aiEnabled ? StageStatus.PENDING : StageStatus.SKIPPED;

        this.analysisStatus = AnalysisStatus.PENDING;
        this.summary = null;
        this.summaryStatus = aiStage;
        this.mainGroupCode = null;
        this.depLevel = null;
        this.depScore = null;
        this.modelVersion = null;
        this.embeddingStatus = aiStage;
        this.embeddingVersion = null;
        this.embeddedAt = null;
        this.analyzedAt = Instant.now();
    }

    public void applyCore(AnalysisStatus status, String mainGroupCode, Integer depLevel, BigDecimal depScore,
                          String modelVersion) {
        this.analysisStatus = status;
        this.mainGroupCode = mainGroupCode;
        this.depLevel = depLevel;
        this.depScore = depScore;
        this.modelVersion = modelVersion;
        this.analyzedAt = Instant.now();
    }

    public void failCore() {
        this.analysisStatus = AnalysisStatus.FAILED;
        this.analyzedAt = Instant.now();
    }

    public void applySummary(String summary) {
        this.summary = summary;
        this.summaryStatus = StageStatus.DONE;
        this.analyzedAt = Instant.now();
    }

    public void failSummary() {
        this.summaryStatus = StageStatus.FAILED;
        this.analyzedAt = Instant.now();
    }

    public void applyEmbedding(String embeddingVersion) {
        this.embeddingStatus = StageStatus.DONE;
        this.embeddingVersion = embeddingVersion;
        this.embeddedAt = Instant.now();
        this.analyzedAt = this.embeddedAt;
    }

    public void failEmbedding() {
        this.embeddingStatus = StageStatus.FAILED;
        this.analyzedAt = Instant.now();
    }
}
