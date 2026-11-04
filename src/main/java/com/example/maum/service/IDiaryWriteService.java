package com.example.maum.service;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.EmotionCatalog.RankedEmotion;
import com.example.maum.dto.AiAnalysisDTO;
import com.example.maum.dto.DiaryAnalysisInputDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

// FastAPI 호출(느린 외부 호출)은 트랜잭션 밖에서 하고, 그 결과를 DB에 쓰는 짧은 부분만 이 서비스가 하나의 트랜잭션으로 처리함.
// apply* 는 분석하는 동안 일기 제목·본문이 바뀐 경우(오래된 결과)에는 저장하지 않고 false 를 돌려줌 — 바뀐 본문은 이미 재분석이 예약되어 있음
public interface IDiaryWriteService {

    Optional<DiaryAnalysisInputDTO> loadAnalysisInput(Long diaryId);

    boolean applyCore(Long diaryId, String title, String content, List<RankedEmotion> ranked,
                      AnalysisStatus status, Integer depLevel, BigDecimal depScore, String modelVersion);

    void failCore(Long diaryId, String title, String content);

    boolean applySummary(Long diaryId, String title, String content, String summary, List<AiAnalysisDTO.Track> tracks);

    void failSummary(Long diaryId, String title, String content);

    boolean applyEmbedding(Long diaryId, String title, String content, String embeddingVersion);

    void failEmbedding(Long diaryId, String title, String content);
}
