package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Spring ↔ FastAPI 분석 계약 (3단계 호출). 파이썬 쪽 필드명이 snake_case 라 JsonProperty 로 맞춘다.
//   1) /api/analyze/core   : 로컬 모델만 (감정 44개 점수 + 우울) → 끝나는 즉시 화면에 결과를 보여줌
//   2) /api/analyze/enrich : Gemini 한 번 (요약 + 음악 키워드) 후 Spotify 검색
//   3) /api/analyze/embed  : 임베딩을 만들어 Mongo 벡터 컬렉션에 저장 (사용자는 기다리지 않음)
public final class AiAnalysisDTO {

    private AiAnalysisDTO() {
    }

    @Builder
    public record CoreRequest(
            @JsonProperty("diary_id") String diaryId,
            String title,
            String content
    ) {
    }

    // emotionScores 의 키는 감정 라벨(emotion.name_ko), 값은 0~1 확률. depSkipped 면 우울 모델을 돌리지 않은 것
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CoreResponse(
            @JsonProperty("emotion_scores") Map<String, Float> emotionScores,
            @JsonProperty("dep_level") Integer depLevel,
            @JsonProperty("dep_score") BigDecimal depScore,
            @JsonProperty("dep_skipped") boolean depSkipped,
            @JsonProperty("model_version") String modelVersion
    ) {
    }

    @Builder
    public record EnrichRequest(
            @JsonProperty("diary_id") String diaryId,
            String title,
            String content,
            @JsonProperty("main_emotion") String mainEmotion,
            @JsonProperty("top_emotions") List<String> topEmotions
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EnrichResponse(
            String summary,
            List<Track> tracks
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Track(
            @JsonProperty("track_id") String trackId,
            @JsonProperty("track_name") String trackName,
            @JsonProperty("artist_name") String artistName,
            @JsonProperty("album_image_url") String albumImageUrl,
            @JsonProperty("spotify_url") String spotifyUrl
    ) {
    }

    @Builder
    public record EmbedRequest(
            @JsonProperty("diary_id") String diaryId,
            @JsonProperty("user_id") String userId,
            @JsonProperty("diary_date") String diaryDate,
            String title,
            String content,
            String summary
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbedResponse(
            @JsonProperty("embedding_version") String embeddingVersion
    ) {
    }
}
