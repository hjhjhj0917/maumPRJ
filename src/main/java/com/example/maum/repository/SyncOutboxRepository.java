package com.example.maum.repository;

import com.example.maum.repository.entity.SyncOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncOutboxRepository extends JpaRepository<SyncOutboxEntity, Long> {

    // 처리할 작업을 잠그고 가져옴(SKIP LOCKED: 서버가 여러 대여도 같은 행을 중복 처리하지 않음).
    // 실패한 행은 attempts² 분이 지난 뒤에 다시 가져옴(1, 4, 9, ... 분 간격으로 물러남)
    @Query(value = "SELECT * FROM sync_outbox "
            + "WHERE attempts < :maxAttempts AND created_at + make_interval(mins => attempts * attempts) <= now() "
            + "ORDER BY id LIMIT :batchSize FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<SyncOutboxEntity> claimDue(@Param("maxAttempts") int maxAttempts, @Param("batchSize") int batchSize);
}
