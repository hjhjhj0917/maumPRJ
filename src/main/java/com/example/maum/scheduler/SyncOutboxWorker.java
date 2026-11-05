package com.example.maum.scheduler;

import com.example.maum.dto.AiAnalysisDTO;
import com.example.maum.repository.SyncOutboxRepository;
import com.example.maum.repository.entity.SyncOutboxEntity;
import com.example.maum.service.IGcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

// sync_outbox 에 쌓인 외부 저장소 삭제 작업(Mongo 벡터, 스토리지 파일)을 처리함.
// 성공하면 행을 지우고, 실패하면 attempts 를 올려 나중에 다시 시도함. MAX_ATTEMPTS 를 넘긴 행은 더 집지 않고 DB 에 남겨 운영에서 확인함
@Slf4j
@RequiredArgsConstructor
@Component
public class SyncOutboxWorker {

    static final int MAX_ATTEMPTS = 12;
    static final int BATCH_SIZE = 20;

    private final SyncOutboxRepository syncOutboxRepository;
    private final IGcsService gcsService;
    private final RestClient pythonApiRestClient;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    // 락을 잡은 채 외부 호출을 하므로 배치를 작게 유지함
    @Transactional
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void processOutbox() {

        log.info("{}.processOutbox Start!", this.getClass().getName());

        try {
            List<SyncOutboxEntity> rows = syncOutboxRepository.claimDue(MAX_ATTEMPTS, BATCH_SIZE);

            int done = 0;
            for (SyncOutboxEntity row : rows) {
                try {
                    handle(row);
                    syncOutboxRepository.delete(row);
                    done++;
                } catch (Exception e) {
                    row.markFailed();
                    log.warn("outbox 처리 실패 id: {}, target: {}, action: {}, attempts: {}, error: {}",
                            row.getId(), row.getTarget(), row.getAction(), row.getAttempts(), e.getMessage());
                }
            }

            log.info("{}.processOutbox End! 대상 {}건, 성공 {}건", this.getClass().getName(), rows.size(), done);
        } catch (Exception e) {
            log.error("processOutbox error", e);
        }
    }

    private void handle(SyncOutboxEntity row) {
        // shortcut: GcsService.deleteImage 는 실패를 로그만 남기고 삼킴, 파일 삭제 재시도가 필요해지면 예외를 던지게 바꿀 것
        if (SyncOutboxEntity.TARGET_STORAGE.equals(row.getTarget()) && SyncOutboxEntity.ACTION_DELETE.equals(row.getAction())) {
            gcsService.deleteImage(row.getRefKey());
            return;
        }

        if (SyncOutboxEntity.TARGET_MONGO.equals(row.getTarget())
                && (SyncOutboxEntity.ACTION_DELETE.equals(row.getAction()) || SyncOutboxEntity.ACTION_PURGE_USER.equals(row.getAction()))) {
            pythonApiRestClient.post()
                    .uri(pythonApiUrl + "/api/vector/delete")
                    .body(AiAnalysisDTO.VectorDeleteRequest.builder().action(row.getAction()).refKey(row.getRefKey()).build())
                    .retrieve()
                    .toBodilessEntity();
            return;
        }

        throw new IllegalStateException("지원하지 않는 outbox 작업입니다: " + row.getTarget() + "/" + row.getAction());
    }
}
