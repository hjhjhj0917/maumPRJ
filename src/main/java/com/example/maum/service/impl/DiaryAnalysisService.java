package com.example.maum.service.impl;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.DiaryAnalysisRequestedEvent;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.diary.EmotionCatalog.RankedEmotion;
import com.example.maum.diary.StageStatus;
import com.example.maum.dto.AiAnalysisDTO;
import com.example.maum.dto.DiaryAnalysisInputDTO;
import com.example.maum.service.IDiaryAnalysisService;
import com.example.maum.service.IDiaryWriteService;
import com.example.maum.service.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/*
일기 분석 파이프라인: 사용자가 오래 기다리지 않도록 3단계로 나눠 각각 끝나는 대로 DB에 반영함.
  1) core   : 로컬 모델(감정 44개 점수 + 우울) — 끝나면 바로 감정 결과를 보여줌
  2) enrich : Gemini 한 번(요약 + 음악 키워드) 후 Spotify 검색
  3) embed  : 임베딩을 만들어 벡터 저장소에 반영 (사용자는 기다리지 않음)
한 단계가 실패해도 앞 단계 결과는 남고, 실패한 단계는 상태(FAILED)로 기록되어 재시도 스케줄러가 이어서 처리함.
프라이버시 모드면 2, 3단계 상태가 처음부터 SKIPPED 라 외부 AI(Gemini)를 호출하지 않음.
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryAnalysisService implements IDiaryAnalysisService {

    private static final int TOP_EMOTION_COUNT = 10;
    private static final String DIARY_CACHE_PREFIX = "diaryCache::";

    private final RestClient pythonApiRestClient;
    private final IDiaryWriteService diaryWriteService;
    private final IRedisService redisService;
    private final EmotionCatalog emotionCatalog;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    // 발행·수정 트랜잭션이 커밋된 뒤에, 요청 스레드와 별도의 스레드에서 실행함 (사용자 응답을 막지 않음).
    // 실행 큐가 가득 차 거부되면 상태가 PENDING 으로 남아 재시도 스케줄러가 처리함
    @Async("analysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnalysisRequested(DiaryAnalysisRequestedEvent event) {
        analyze(event.diaryId());
    }

    @Override
    public void analyze(Long diaryId) {

        log.info("{}.analyze Start! diaryId: {}", this.getClass().getName(), diaryId);

        Optional<DiaryAnalysisInputDTO> oInput = diaryWriteService.loadAnalysisInput(diaryId);

        if (oInput.isEmpty()) {
            log.info("분석할 일기가 없거나 발행 상태가 아님 (diaryId: {})", diaryId);
            return;
        }

        DiaryAnalysisInputDTO input = oInput.get();
        List<String> topEmotions = input.topEmotions();

        if (!input.analysisStatus().isFinished()) {
            Optional<List<String>> coreResult = runCore(input);

            if (coreResult.isEmpty()) {
                return;
            }

            topEmotions = coreResult.get();
        }

        String summary = input.summary();

        if (needsRun(input.summaryStatus())) {
            Optional<String> enrichResult = runEnrich(input, topEmotions);

            if (enrichResult.isEmpty()) {
                return;
            }

            summary = enrichResult.get();
        }

        // 임베딩은 요약이 만들어진 뒤에만 가능함 (요약도 임베딩 입력에 포함)
        if (needsRun(input.embeddingStatus()) && summary != null) {
            runEmbed(input, summary);
        }

        log.info("{}.analyze End! diaryId: {}", this.getClass().getName(), diaryId);
    }

    private boolean needsRun(StageStatus status) {
        return status == StageStatus.PENDING || status == StageStatus.FAILED;
    }

    // 1단계. 성공하면 상위 감정 이름을, 실패하거나 분석 중 일기가 바뀌어 저장하지 않았으면 빈 값을 돌려줌
    private Optional<List<String>> runCore(DiaryAnalysisInputDTO input) {

        try {
            AiAnalysisDTO.CoreResponse res = pythonApiRestClient.post()
                    .uri(pythonApiUrl + "/api/analyze/core")
                    .body(AiAnalysisDTO.CoreRequest.builder()
                            .diaryId(input.diaryPublicId())
                            .title(input.title())
                            .content(input.content())
                            .build())
                    .retrieve()
                    .body(AiAnalysisDTO.CoreResponse.class);

            if (res == null || res.emotionScores() == null) {
                throw new IllegalStateException("코어 분석 응답에 감정 점수가 없음");
            }

            List<RankedEmotion> ranked = emotionCatalog.rank(res.emotionScores());

            if (ranked.isEmpty()) {
                throw new IllegalStateException("코어 분석 응답에서 알 수 있는 감정 라벨이 하나도 없음");
            }

            AnalysisStatus status = AnalysisStatus.DONE;
            Integer depLevel = res.depLevel();

            if (res.depSkipped()) {
                // 우울 모델을 돌리지 않은 경우: 지수는 실제 확률이 아니므로 비우고 수준은 0 (DB 제약과 동일)
                status = AnalysisStatus.DEP_SKIPPED;
                depLevel = 0;
            } else if (depLevel == null || res.depScore() == null) {
                throw new IllegalStateException("코어 분석 응답에 우울 결과가 없음");
            }

            boolean applied = diaryWriteService.applyCore(input.diaryId(), input.title(), input.content(), ranked,
                    status, depLevel, res.depSkipped() ? null : res.depScore(), res.modelVersion());

            if (!applied) {
                return Optional.empty();
            }

            // 목록 색상이 분석 결과로 바뀌므로 사용자의 최근 목록 캐시를 비움
            redisService.deleteValues(DIARY_CACHE_PREFIX + input.userPublicId());

            return Optional.of(ranked.stream()
                    .limit(TOP_EMOTION_COUNT)
                    .map(r -> r.emotion().getNameKo())
                    .toList());

        } catch (Exception e) {
            log.error("코어 분석 실패 (diaryId: {}): {}", input.diaryId(), e.getMessage());
            diaryWriteService.failCore(input.diaryId(), input.title(), input.content());
            return Optional.empty();
        }
    }

    // 2단계. 성공하면 요약을, 실패하거나 저장하지 않았으면 빈 값을 돌려줌
    private Optional<String> runEnrich(DiaryAnalysisInputDTO input, List<String> topEmotions) {

        try {
            AiAnalysisDTO.EnrichResponse res = pythonApiRestClient.post()
                    .uri(pythonApiUrl + "/api/analyze/enrich")
                    .body(AiAnalysisDTO.EnrichRequest.builder()
                            .diaryId(input.diaryPublicId())
                            .title(input.title())
                            .content(input.content())
                            .mainEmotion(topEmotions.isEmpty() ? null : topEmotions.get(0))
                            .topEmotions(topEmotions)
                            .build())
                    .retrieve()
                    .body(AiAnalysisDTO.EnrichResponse.class);

            if (res == null || res.summary() == null || res.summary().isBlank()) {
                throw new IllegalStateException("요약 응답이 비어 있음");
            }

            boolean applied = diaryWriteService.applySummary(
                    input.diaryId(), input.title(), input.content(), res.summary(), res.tracks());

            return applied ? Optional.of(res.summary()) : Optional.empty();

        } catch (Exception e) {
            log.error("요약·음악 생성 실패 (diaryId: {}): {}", input.diaryId(), e.getMessage());
            diaryWriteService.failSummary(input.diaryId(), input.title(), input.content());
            return Optional.empty();
        }
    }

    // 3단계. 실패해도 사용자가 보는 결과에는 영향이 없어 상태만 기록함
    private void runEmbed(DiaryAnalysisInputDTO input, String summary) {

        try {
            AiAnalysisDTO.EmbedResponse res = pythonApiRestClient.post()
                    .uri(pythonApiUrl + "/api/analyze/embed")
                    .body(AiAnalysisDTO.EmbedRequest.builder()
                            .diaryId(input.diaryPublicId())
                            .userId(input.userPublicId())
                            .diaryDate(input.diaryDate())
                            .title(input.title())
                            .content(input.content())
                            .summary(summary)
                            .build())
                    .retrieve()
                    .body(AiAnalysisDTO.EmbedResponse.class);

            diaryWriteService.applyEmbedding(input.diaryId(), input.title(), input.content(),
                    res == null ? null : res.embeddingVersion());

        } catch (Exception e) {
            log.error("임베딩 저장 실패 (diaryId: {}): {}", input.diaryId(), e.getMessage());
            diaryWriteService.failEmbedding(input.diaryId(), input.title(), input.content());
        }
    }
}
