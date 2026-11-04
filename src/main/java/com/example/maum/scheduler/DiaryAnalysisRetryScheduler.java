package com.example.maum.scheduler;

import com.example.maum.repository.DiaryAnalysisRepository;
import com.example.maum.service.IDiaryAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

// 실패했거나 서버 재시작 등으로 멈춘 분석을 이어서 처리함. 끝난 단계는 건너뛰고 남은 단계만 실행됨.
// 상태가 바뀐 지 5분이 안 된 건은 진행 중일 수 있어 제외하고, 24시간이 지난 건은 포기함(재시도 횟수 컬럼 없이 시간으로 제한)
@Slf4j
@RequiredArgsConstructor
@Component
public class DiaryAnalysisRetryScheduler {

    private static final Duration MIN_AGE = Duration.ofMinutes(5);
    private static final Duration MAX_AGE = Duration.ofHours(24);
    private static final int BATCH_SIZE = 20;

    private final DiaryAnalysisRepository diaryAnalysisRepository;
    private final IDiaryAnalysisService diaryAnalysisService;

    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void retryUnfinishedAnalysis() {

        log.info("미완료 일기 분석 재시도 시작");

        try {
            Instant now = Instant.now();

            List<Long> diaryIds = diaryAnalysisRepository.findRetryTargets(
                    now.minus(MAX_AGE), now.minus(MIN_AGE), PageRequest.of(0, BATCH_SIZE));

            for (Long diaryId : diaryIds) {
                diaryAnalysisService.analyze(diaryId);
            }

            log.info("미완료 일기 분석 재시도 완료: {}건", diaryIds.size());
        } catch (Exception e) {
            log.error("retryUnfinishedAnalysis error", e);
        }
    }
}
