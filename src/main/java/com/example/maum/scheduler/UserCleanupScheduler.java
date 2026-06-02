package com.example.maum.scheduler;

import com.example.maum.repository.DiaryLogRepository;
import com.example.maum.repository.UserInfoRepository;
import com.example.maum.repository.entity.UserInfoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserCleanupScheduler {

    private final UserInfoRepository userInfoRepository;
    private final DiaryLogRepository diaryLogRepository;

    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupWithdrawnUsers() {

        log.info("탈퇴 후 10일 경과 유저(MySQL) 및 일기 로그(MongoDB) 삭제 시작");

        try {
            List<UserInfoEntity> users = userInfoRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (UserInfoEntity user : users) {
                if ("N".equals(user.getUserStatus()) && user.getUpdatedAt() != null) {
                    String updatedAtStr = user.getUpdatedAt().length() > 19
                            ? user.getUpdatedAt().substring(0, 19)
                            : user.getUpdatedAt();
                    LocalDateTime updatedAt = LocalDateTime.parse(updatedAtStr, formatter);

                    if (ChronoUnit.DAYS.between(updatedAt, now) >= 10) {

                        diaryLogRepository.deleteByUserNo(Integer.valueOf(user.getUserNo()));

                        userInfoRepository.delete(user);

                        log.info("탈퇴 후 10일 경과 유저(MySQL) 및 일기 로그(MongoDB) 삭제 완료: {}", user.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("cleanupWithdrawnUsers error", e);
        }
    }
}