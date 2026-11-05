package com.example.maum.scheduler;

import com.example.maum.auth.UserStatus;
import com.example.maum.repository.DiaryImageRepository;
import com.example.maum.repository.SyncOutboxRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.SyncOutboxEntity;
import com.example.maum.repository.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final SyncOutboxRepository syncOutboxRepository;

    // users 행을 지우면 하위 테이블은 DB CASCADE 가 정리함. DB 밖에 있는 데이터(Mongo 벡터, 스토리지 파일)는
    // 지울 대상을 sync_outbox 에 같은 트랜잭션으로 남겨 SyncOutboxWorker 가 처리하게 함
    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupWithdrawnUsers() {

        log.info("탈퇴 유예 기간이 끝난 유저 삭제 시작");

        try {
            Instant now = Instant.now();

            List<UserEntity> targets = userRepository.findByStatusAndPurgeAtLessThanEqual(UserStatus.WITHDRAWN_PENDING, now);

            if (!targets.isEmpty()) {
                syncOutboxRepository.saveAll(buildOutboxRows(targets));
            }

            int deletedCount = userRepository.deleteWithdrawnUsersPurgedUntil(now);

            log.info("탈퇴 유예 기간이 끝난 유저 삭제 완료: {}명", deletedCount);
        } catch (Exception e) {
            log.error("cleanupWithdrawnUsers error", e);
            // 일부만 반영되지 않도록 outbox 기록과 삭제를 함께 되돌림
            throw e;
        }
    }

    private List<SyncOutboxEntity> buildOutboxRows(List<UserEntity> users) {
        List<SyncOutboxEntity> rows = new ArrayList<>();

        for (UserEntity user : users) {
            rows.add(outbox(SyncOutboxEntity.TARGET_MONGO, SyncOutboxEntity.ACTION_PURGE_USER, user.getPublicId().toString()));

            if (user.getProfileImageKey() != null) {
                rows.add(outbox(SyncOutboxEntity.TARGET_STORAGE, SyncOutboxEntity.ACTION_DELETE, user.getProfileImageKey()));
            }
        }

        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        for (String storageKey : diaryImageRepository.findStorageKeysByUserIds(userIds)) {
            rows.add(outbox(SyncOutboxEntity.TARGET_STORAGE, SyncOutboxEntity.ACTION_DELETE, storageKey));
        }

        return rows;
    }

    private SyncOutboxEntity outbox(String target, String action, String refKey) {
        return SyncOutboxEntity.builder().target(target).action(action).refKey(refKey).build();
    }
}
