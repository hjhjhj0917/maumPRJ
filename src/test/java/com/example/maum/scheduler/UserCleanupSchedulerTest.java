package com.example.maum.scheduler;

import com.example.maum.auth.UserStatus;
import com.example.maum.repository.DiaryImageRepository;
import com.example.maum.repository.SyncOutboxRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.SyncOutboxEntity;
import com.example.maum.repository.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// DB 없이 탈퇴 정리가 users 행을 지우기 전에 외부 삭제 대상(벡터, 프로필/일기 이미지)을 outbox 에 남기는지 확인함
class UserCleanupSchedulerTest {

    UserRepository userRepository = mock(UserRepository.class);
    DiaryImageRepository diaryImageRepository = mock(DiaryImageRepository.class);
    SyncOutboxRepository syncOutboxRepository = mock(SyncOutboxRepository.class);
    UserCleanupScheduler scheduler = new UserCleanupScheduler(userRepository, diaryImageRepository, syncOutboxRepository);

    @Test
    void 삭제_전에_벡터와_이미지_삭제_작업을_outbox_에_남긴다() {
        UUID publicId = UUID.randomUUID();
        UserEntity user = UserEntity.builder().id(5L).publicId(publicId).profileImageKey("profile/5.png").build();
        when(userRepository.findByStatusAndPurgeAtLessThanEqual(any(UserStatus.class), any(Instant.class)))
                .thenReturn(List.of(user));
        when(diaryImageRepository.findStorageKeysByUserIds(List.of(5L))).thenReturn(List.of("diary/a.png"));

        scheduler.cleanupWithdrawnUsers();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<SyncOutboxEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        var order = inOrder(syncOutboxRepository, userRepository);
        order.verify(syncOutboxRepository).saveAll(captor.capture());
        order.verify(userRepository).deleteWithdrawnUsersPurgedUntil(any(Instant.class));

        List<String> rows = new ArrayList<>();
        captor.getValue().forEach(r -> rows.add(r.getTarget() + ":" + r.getAction() + ":" + r.getRefKey()));
        assertEquals(List.of("MONGO:PURGE_USER:" + publicId, "STORAGE:DELETE:profile/5.png", "STORAGE:DELETE:diary/a.png"), rows);
    }

    @Test
    void 대상이_없으면_outbox_를_건드리지_않는다() {
        when(userRepository.findByStatusAndPurgeAtLessThanEqual(any(UserStatus.class), any(Instant.class)))
                .thenReturn(List.of());

        scheduler.cleanupWithdrawnUsers();

        verify(syncOutboxRepository, never()).saveAll(any());
    }
}
