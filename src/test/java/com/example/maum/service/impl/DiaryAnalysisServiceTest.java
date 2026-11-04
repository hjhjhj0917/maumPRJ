package com.example.maum.service.impl;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.diary.EmotionCatalog.RankedEmotion;
import com.example.maum.diary.StageStatus;
import com.example.maum.dto.DiaryAnalysisInputDTO;
import com.example.maum.repository.entity.EmotionEntity;
import com.example.maum.service.IDiaryWriteService;
import com.example.maum.service.IRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// DB·FastAPI 없이 분석 파이프라인의 단계 진행(3단계 호출, 실패 격리, 이어서 하기)과 Spring↔FastAPI 계약(요청·응답 필드)을 확인함.
// FastAPI 는 MockRestServiceServer 가 만든 가짜 응답으로 대체하고, DB 저장은 가짜 IDiaryWriteService 로 대체함
class DiaryAnalysisServiceTest {

    private static final String AI_URL = "http://ai-server";
    private static final long DIARY_ID = 7L;
    private static final String DIARY_PUBLIC_ID = "11111111-1111-1111-1111-111111111111";
    private static final String USER_PUBLIC_ID = "22222222-2222-2222-2222-222222222222";

    private static final String CORE_OK = """
            {"emotion_scores": {"슬픔": 0.91, "힘듦/지침": 0.62}, "dep_level": 1, "dep_score": 0.7312,
             "dep_skipped": false, "model_version": "kote-v1"}""";
    private static final String ENRICH_OK = """
            {"summary": "힘든 하루였어요.",
             "tracks": [{"track_id": "t1", "track_name": "곡", "artist_name": "가수",
                         "album_image_url": "http://img", "spotify_url": "http://spotify/t1"}]}""";
    private static final String EMBED_OK = """
            {"embedding_version": "gemini-embedding-001"}""";

    IDiaryWriteService writeService = mock(IDiaryWriteService.class);
    IRedisService redisService = mock(IRedisService.class);
    EmotionCatalog emotionCatalog = mock(EmotionCatalog.class);

    MockRestServiceServer server;
    DiaryAnalysisService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        service = new DiaryAnalysisService(builder.build(), writeService, redisService, emotionCatalog);
        ReflectionTestUtils.setField(service, "pythonApiUrl", AI_URL);

        when(emotionCatalog.rank(any())).thenReturn(List.of(
                new RankedEmotion(emotion((short) 22, "슬픔", "SADNESS"), 0.91f),
                new RankedEmotion(emotion((short) 28, "힘듦/지침", "SADNESS"), 0.62f)));
    }

    private EmotionEntity emotion(short id, String nameKo, String groupCode) {
        EmotionEntity emotion = new EmotionEntity();
        ReflectionTestUtils.setField(emotion, "id", id);
        ReflectionTestUtils.setField(emotion, "nameKo", nameKo);
        ReflectionTestUtils.setField(emotion, "groupCode", groupCode);
        return emotion;
    }

    private DiaryAnalysisInputDTO input(AnalysisStatus analysis, StageStatus summaryStatus, StageStatus embeddingStatus,
                                        String summary, List<String> topEmotions) {
        return DiaryAnalysisInputDTO.builder()
                .diaryId(DIARY_ID)
                .diaryPublicId(DIARY_PUBLIC_ID)
                .userPublicId(USER_PUBLIC_ID)
                .diaryDate("2026-11-01")
                .title("슬픈 날")
                .content("오늘은 많이 힘들었다.")
                .analysisStatus(analysis)
                .summaryStatus(summaryStatus)
                .embeddingStatus(embeddingStatus)
                .summary(summary)
                .topEmotions(topEmotions)
                .build();
    }

    private void givenInput(DiaryAnalysisInputDTO input) {
        when(writeService.loadAnalysisInput(DIARY_ID)).thenReturn(Optional.of(input));
    }

    @Test
    void 세_단계를_차례로_호출하고_각_결과를_저장한다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core")).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.diary_id").value(DIARY_PUBLIC_ID))
                .andExpect(jsonPath("$.title").value("슬픈 날"))
                .andRespond(withSuccess(CORE_OK, MediaType.APPLICATION_JSON));
        server.expect(requestTo(AI_URL + "/api/analyze/enrich")).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.main_emotion").value("슬픔"))
                .andExpect(jsonPath("$.top_emotions[1]").value("힘듦/지침"))
                .andRespond(withSuccess(ENRICH_OK, MediaType.APPLICATION_JSON));
        server.expect(requestTo(AI_URL + "/api/analyze/embed")).andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.user_id").value(USER_PUBLIC_ID))
                .andExpect(jsonPath("$.diary_date").value("2026-11-01"))
                .andExpect(jsonPath("$.summary").value("힘든 하루였어요."))
                .andRespond(withSuccess(EMBED_OK, MediaType.APPLICATION_JSON));

        when(writeService.applyCore(eq(DIARY_ID), any(), any(), anyList(), any(), any(), any(), any())).thenReturn(true);
        when(writeService.applySummary(eq(DIARY_ID), any(), any(), any(), any())).thenReturn(true);

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService).applyCore(eq(DIARY_ID), eq("슬픈 날"), eq("오늘은 많이 힘들었다."), anyList(),
                eq(AnalysisStatus.DONE), eq(1), eq(new BigDecimal("0.7312")), eq("kote-v1"));
        verify(writeService).applySummary(eq(DIARY_ID), any(), any(), eq("힘든 하루였어요."), anyList());
        verify(writeService).applyEmbedding(DIARY_ID, "슬픈 날", "오늘은 많이 힘들었다.", "gemini-embedding-001");
        // 목록 색상이 바뀌므로 사용자의 최근 목록 캐시를 비움
        verify(redisService).deleteValues("diaryCache::" + USER_PUBLIC_ID);
    }

    @Test
    void 우울_모델을_생략한_응답은_DEP_SKIPPED_로_저장한다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.SKIPPED, StageStatus.SKIPPED, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core"))
                .andRespond(withSuccess("""
                        {"emotion_scores": {"행복": 0.8}, "dep_skipped": true, "model_version": "kote-v1"}""",
                        MediaType.APPLICATION_JSON));
        when(writeService.applyCore(eq(DIARY_ID), any(), any(), anyList(), any(), any(), any(), any())).thenReturn(true);

        service.analyze(DIARY_ID);

        // 생략이면 우울 수준은 0, 지수는 비움 (DB 제약 ck_analysis_dep_skipped 와 동일)
        verify(writeService).applyCore(eq(DIARY_ID), any(), any(), anyList(),
                eq(AnalysisStatus.DEP_SKIPPED), eq(0), eq(null), eq("kote-v1"));
        server.verify();
    }

    @Test
    void 코어_분석이_실패하면_실패로_기록하고_뒤_단계는_호출하지_않는다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService).failCore(DIARY_ID, "슬픈 날", "오늘은 많이 힘들었다.");
        verify(writeService, never()).applySummary(any(), any(), any(), any(), any());
    }

    @Test
    void 요약_생성이_실패해도_감정_결과는_남고_임베딩은_건너뛴다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core")).andRespond(withSuccess(CORE_OK, MediaType.APPLICATION_JSON));
        server.expect(requestTo(AI_URL + "/api/analyze/enrich")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        when(writeService.applyCore(eq(DIARY_ID), any(), any(), anyList(), any(), any(), any(), any())).thenReturn(true);

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService).failSummary(DIARY_ID, "슬픈 날", "오늘은 많이 힘들었다.");
        verify(writeService, never()).failCore(any(), any(), any());
        verify(writeService, never()).applyEmbedding(any(), any(), any(), any());
    }

    @Test
    void 임베딩만_실패한_건은_임베딩_단계만_다시_호출한다() {
        givenInput(input(AnalysisStatus.DONE, StageStatus.DONE, StageStatus.FAILED, "저장된 요약", List.of("슬픔")));

        server.expect(requestTo(AI_URL + "/api/analyze/embed"))
                .andExpect(jsonPath("$.summary").value("저장된 요약"))
                .andRespond(withSuccess(EMBED_OK, MediaType.APPLICATION_JSON));

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService).applyEmbedding(DIARY_ID, "슬픈 날", "오늘은 많이 힘들었다.", "gemini-embedding-001");
        verify(writeService, never()).applyCore(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void 요약이_실패한_건을_재시도하면_저장된_상위_감정으로_요약부터_이어서_한다() {
        givenInput(input(AnalysisStatus.DONE, StageStatus.FAILED, StageStatus.PENDING, null, List.of("슬픔", "힘듦/지침")));

        server.expect(requestTo(AI_URL + "/api/analyze/enrich"))
                .andExpect(jsonPath("$.main_emotion").value("슬픔"))
                .andRespond(withSuccess(ENRICH_OK, MediaType.APPLICATION_JSON));
        server.expect(requestTo(AI_URL + "/api/analyze/embed")).andRespond(withSuccess(EMBED_OK, MediaType.APPLICATION_JSON));
        when(writeService.applySummary(eq(DIARY_ID), any(), any(), any(), any())).thenReturn(true);

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService, never()).applyCore(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void 프라이버시_모드로_외부_AI_단계가_SKIPPED_면_코어_분석만_한다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.SKIPPED, StageStatus.SKIPPED, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core")).andRespond(withSuccess(CORE_OK, MediaType.APPLICATION_JSON));
        when(writeService.applyCore(eq(DIARY_ID), any(), any(), anyList(), any(), any(), any(), any())).thenReturn(true);

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService, never()).applySummary(any(), any(), any(), any(), any());
        verify(writeService, never()).applyEmbedding(any(), any(), any(), any());
    }

    @Test
    void 분석하는_동안_일기가_바뀌어_결과를_저장하지_못하면_실패로_기록하지_않고_멈춘다() {
        givenInput(input(AnalysisStatus.PENDING, StageStatus.PENDING, StageStatus.PENDING, null, List.of()));

        server.expect(requestTo(AI_URL + "/api/analyze/core")).andRespond(withSuccess(CORE_OK, MediaType.APPLICATION_JSON));
        when(writeService.applyCore(eq(DIARY_ID), any(), any(), anyList(), any(), any(), any(), any())).thenReturn(false);

        service.analyze(DIARY_ID);

        server.verify();
        verify(writeService, never()).failCore(any(), any(), any());
        verify(writeService, never()).applySummary(any(), any(), any(), any(), any());
        verify(redisService, never()).deleteValues(any());
    }

    @Test
    void 삭제되었거나_발행되지_않은_일기는_아무것도_호출하지_않는다() {
        when(writeService.loadAnalysisInput(DIARY_ID)).thenReturn(Optional.empty());

        service.analyze(DIARY_ID);

        server.verify();
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(writeService).loadAnalysisInput(captor.capture());
        assertEquals(DIARY_ID, captor.getValue());
    }
}
