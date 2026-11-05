package com.example.maum.repository;

import com.example.maum.repository.entity.ChatSessionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, Long> {

    Optional<ChatSessionEntity> findByPublicIdAndUserId(UUID publicId, Long userId);

    List<ChatSessionEntity> findByUserIdOrderByPinnedDescUpdatedAtDesc(Long userId);

    // seq 채번과 스트리밍 중복 방지를 위해 같은 세션의 동시 요청을 줄 세움
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ChatSessionEntity s WHERE s.publicId = :publicId AND s.userId = :userId")
    Optional<ChatSessionEntity> findForUpdate(@Param("publicId") UUID publicId, @Param("userId") Long userId);

    long deleteByPublicIdAndUserId(UUID publicId, Long userId);
}
