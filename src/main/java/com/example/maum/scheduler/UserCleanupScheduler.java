package com.example.maum.scheduler;

import com.example.maum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // shortcut: users 행만 지움(하위 테이블은 DB CASCADE). 일기 벡터(Mongo) 삭제는 sync_outbox 도입(4단계) 때 이어서 붙임
    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupWithdrawnUsers() {

        log.info("탈퇴 유예 기간이 끝난 유저 삭제 시작");

        try {
            int deletedCount = userRepository.deleteWithdrawnUsersPurgedUntil(Instant.now());

            log.info("탈퇴 유예 기간이 끝난 유저 삭제 완료: {}명", deletedCount);
        } catch (Exception e) {
            log.error("cleanupWithdrawnUsers error", e);
        }
    }
}
