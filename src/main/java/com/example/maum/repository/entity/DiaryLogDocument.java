package com.example.maum.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "DIARY_LOGS")
public class DiaryLogDocument {

    @Id
    private String id;

    @Field("DIARY_NO")
    private Integer diaryNo;

    @Field("USER_NO")
    private Integer userNo;

    @Field("TITLE")
    private String title;

    @Field("CONTENT")
    private String content;

    @Field("EMBEDDING")
    private List<Double> embedding;

    @Field("MAIN_EMOTION")
    private String mainEmotion;

    @Field("ANALYSIS_SUM")
    private String analysisSum;

    @Field("EMO_RES")
    private List<EmotionResult> emoRes;

    @Field("DEP_RES")
    private DepressionResult depRes;

    @Field("REG_DT")
    private LocalDateTime regDt;

    @Field("CHG_DT")
    private LocalDateTime chgDt;

    @Field("VERSION")
    private String version;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionResult {
        @Field("EMOTION")
        private String emotion;

        @Field("SCORE")
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepressionResult {
        @Field("DISEASE_TYPE")
        private String diseaseType;

        @Field("DEP_LVL")
        private Integer depLvl;

        @Field("DEP_SCORE")
        private Double depScore;

        @Field("IS_SYMPTOM")
        private Boolean isSymptom;
    }
}