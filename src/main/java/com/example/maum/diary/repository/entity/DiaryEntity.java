package com.example.maum.diary.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DIARY")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class DiaryEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DIARY_NO")
    private Integer diaryNo;

    @Column(name = "USER_NO", nullable = false)
    private Integer userNo;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "SUMMARY", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "MAIN_EMOTION", length = 50)
    private String mainEmotion;

    @Column(name = "EMOTION_COLOR", length = 7)
    private String emotionColor;

    @Column(name = "DEP_LVL")
    private Integer depLvl;

    @Column(name = "DEP_SCORE", precision = 5, scale = 2)
    private BigDecimal depScore;

    @Column(name = "SYMPTOM_YN")
    private Integer symptomYn;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDate createdAt;

    public void updateAnalysisResult(String summary, String mainEmotion, String emotionColor, Integer depLvl, BigDecimal depScore, Integer symptomYn) {
        this.summary = summary;
        this.mainEmotion = mainEmotion;
        this.emotionColor = emotionColor;
        this.depLvl = depLvl;
        this.depScore = depScore;
        this.symptomYn = symptomYn;
    }
}