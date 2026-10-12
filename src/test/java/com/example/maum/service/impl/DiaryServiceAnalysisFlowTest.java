package com.example.maum.service.impl;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.repository.DiaryImageRepository;
import com.example.maum.repository.DiaryLogRepository;
import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.service.IDiaryWriteService;
import com.example.maum.service.IGcsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 일기 저장/수정 → Python 분석(+음악 추천) 요청 → 결과를 DiaryWriteService로 넘기는 흐름 확인.
// 외부(Python 서버, DB, MongoDB)는 모두 Mockito 가짜로 대체하고, DiaryService의 흐름 제어 코드만 실제로 실행함
class DiaryServiceAnalysisFlowTest {

    DiaryRepository diaryRepository;
    IDiaryWriteService diaryWriteService;
    RestClient restClient;
    DiaryService diaryService;

    @BeforeEach
    void setUp() throws Exception {
        diaryRepository = Mockito.mock(DiaryRepository.class);
        diaryWriteService = Mockito.mock(IDiaryWriteService.class);
        restClient = Mockito.mock(RestClient.class, Mockito.RETURNS_DEEP_STUBS);

        diaryService = new DiaryService(
                diaryRepository,
                Mockito.mock(DiaryLogRepository.class),
                Mockito.mock(DiaryImageRepository.class),
                Mockito.mock(DiaryMusicRepository.class),
                Mockito.mock(IGcsService.class),
                Mockito.mock(MongoTemplate.class),
                restClient,
                diaryWriteService
        );

        // @Value로 주입되는 필드라 테스트에서는 직접 채움
        Field url = DiaryService.class.getDeclaredField("pythonApiUrl");
        url.setAccessible(true);
        url.set(diaryService, "http://python-test");
    }

    private void stubPythonResponse(ResponseEntity<Map> response) {
        Mockito.when(restClient.post()
                        .uri(ArgumentMatchers.anyString())
                        .body(ArgumentMatchers.<Object>any())
                        .retrieve()
                        .toEntity(Map.class))
                .thenReturn(response);
    }

    private void stubPythonFailure() {
        Mockito.when(restClient.post()
                        .uri(ArgumentMatchers.anyString())
                        .body(ArgumentMatchers.<Object>any())
                        .retrieve()
                        .toEntity(Map.class))
                .thenThrow(new RuntimeException("Python 서버 연결 실패(테스트용)"));
    }

    private static Map<String, Object> analysisBody() {
        return Map.of(
                "analysis_summary", "지친 하루였지만 마음이 편해졌어요.",
                "main_emotion", "힘듦/지침",
                "main_color", "#1E3A8A",
                "dep_res", Map.of("final_level", 1, "raw_score", 0.8734, "is_symptom", true),
                "tracks", List.of(
                        Map.of("trackId", "t1", "trackName", "꿈속에서", "artistName", "Acoustic Paradiso",
                                "albumImageUrl", "https://img/1", "spotifyUrl", "https://spotify/1"),
                        Map.of("trackId", "t2", "trackName", "빗소리", "artistName", "Yoon Do Hyun",
                                "albumImageUrl", "https://img/2", "spotifyUrl", "https://spotify/2")
                )
        );
    }

    private static DiaryEntity savedEntity(int diaryNo, String userNo) {
        return DiaryEntity.builder()
                .diaryNo(diaryNo)
                .userNo(userNo)
                .title("제목")
                .content("내용")
                .createdAt(LocalDate.of(2026, 10, 8))
                .build();
    }

    private static DiaryDTO dto(Integer diaryNo, String userNo) {
        return new DiaryDTO(diaryNo, userNo, "제목", "내용", null, null, null, null, null, null,
                "2026-10-08", null, null, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void 일기_저장후_분석결과와_추천곡을_DiaryWriteService로_한번에_넘긴다() throws Exception {
        Mockito.when(diaryRepository.save(ArgumentMatchers.any(DiaryEntity.class)))
                .thenReturn(savedEntity(15, "7"));
        stubPythonResponse(ResponseEntity.ok(analysisBody()));

        int res = diaryService.diaryInsert(dto(null, "7"));

        assertEquals(15, res, "저장된 일기 번호를 반환해야 함");

        ArgumentCaptor<List<Map<String, Object>>> tracks = ArgumentCaptor.forClass(List.class);
        Mockito.verify(diaryWriteService).applyAnalysisResult(
                ArgumentMatchers.eq(15),
                ArgumentMatchers.eq("지친 하루였지만 마음이 편해졌어요."),
                ArgumentMatchers.eq("힘듦/지침"),
                ArgumentMatchers.eq("#1E3A8A"),
                ArgumentMatchers.eq(1),
                ArgumentMatchers.eq(new BigDecimal("0.8734")),
                ArgumentMatchers.eq(1),
                tracks.capture()
        );

        assertEquals(2, tracks.getValue().size());
        assertEquals("꿈속에서", tracks.getValue().get(0).get("trackName"));
        assertEquals("t2", tracks.getValue().get(1).get("trackId"));
    }

    @Test
    void 우울증상_false면_symptomYn은_0으로_변환된다() throws Exception {
        Mockito.when(diaryRepository.save(ArgumentMatchers.any(DiaryEntity.class)))
                .thenReturn(savedEntity(16, "7"));
        stubPythonResponse(ResponseEntity.ok(Map.of(
                "analysis_summary", "평온한 하루",
                "main_emotion", "행복",
                "main_color", "#FFD700",
                "dep_res", Map.of("final_level", 0, "raw_score", 0.05, "is_symptom", false),
                "tracks", List.of()
        )));

        diaryService.diaryInsert(dto(null, "7"));

        Mockito.verify(diaryWriteService).applyAnalysisResult(
                ArgumentMatchers.eq(16), ArgumentMatchers.anyString(), ArgumentMatchers.eq("행복"),
                ArgumentMatchers.eq("#FFD700"), ArgumentMatchers.eq(0), ArgumentMatchers.eq(new BigDecimal("0.05")),
                ArgumentMatchers.eq(0), ArgumentMatchers.eq(List.of())
        );
    }

    @Test
    void Python_서버가_실패해도_일기_저장은_성공으로_처리되고_분석결과는_저장하지_않는다() throws Exception {
        Mockito.when(diaryRepository.save(ArgumentMatchers.any(DiaryEntity.class)))
                .thenReturn(savedEntity(17, "7"));
        stubPythonFailure();

        int res = diaryService.diaryInsert(dto(null, "7"));

        assertEquals(17, res, "분석이 실패해도 일기 번호를 반환해야 함");
        Mockito.verify(diaryWriteService, Mockito.never()).applyAnalysisResult(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void 응답_형식이_깨져도_예외없이_넘어가고_저장하지_않는다() throws Exception {
        Mockito.when(diaryRepository.save(ArgumentMatchers.any(DiaryEntity.class)))
                .thenReturn(savedEntity(18, "7"));
        // dep_res가 없는 비정상 응답
        stubPythonResponse(ResponseEntity.ok(Map.of("analysis_summary", "요약만 있음")));

        int res = diaryService.diaryInsert(dto(null, "7"));

        assertEquals(18, res);
        Mockito.verify(diaryWriteService, Mockito.never()).applyAnalysisResult(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void 일기_수정은_내용_반영_후_재분석_순서로_실행된다() throws Exception {
        Mockito.when(diaryRepository.findById(15)).thenReturn(Optional.of(savedEntity(15, "7")));
        stubPythonResponse(ResponseEntity.ok(analysisBody()));

        MsgDTO result = diaryService.diaryUpdate(dto(15, "7"));

        assertEquals(1, result.result());

        InOrder order = Mockito.inOrder(diaryWriteService);
        order.verify(diaryWriteService).updateDiaryContent(15, "제목", "내용");
        order.verify(diaryWriteService).applyAnalysisResult(
                ArgumentMatchers.eq(15), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyInt(), ArgumentMatchers.any(BigDecimal.class),
                ArgumentMatchers.anyInt(), ArgumentMatchers.anyList());
    }

    @Test
    void 다른_사람의_일기는_수정하지_않고_분석도_요청하지_않는다() throws Exception {
        Mockito.when(diaryRepository.findById(15)).thenReturn(Optional.of(savedEntity(15, "7")));

        MsgDTO result = diaryService.diaryUpdate(dto(15, "999"));

        assertEquals(0, result.result());
        assertNotNull(result.msg());
        Mockito.verifyNoInteractions(diaryWriteService);
    }

    @Test
    void 존재하지_않는_일기_수정은_실패로_응답한다() throws Exception {
        Mockito.when(diaryRepository.findById(404)).thenReturn(Optional.empty());

        MsgDTO result = diaryService.diaryUpdate(dto(404, "7"));

        assertEquals(0, result.result());
        Mockito.verifyNoInteractions(diaryWriteService);
    }
}
