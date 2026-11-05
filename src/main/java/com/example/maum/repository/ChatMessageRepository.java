package com.example.maum.repository;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.chat.ChatRole;
import com.example.maum.repository.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findBySessionIdOrderBySeqAsc(Long sessionId);

    // Gemini 에 넘길 최근 대화(최신순으로 오므로 서비스에서 뒤집어 씀)
    List<ChatMessageEntity> findTop8BySessionIdAndStatusInOrderBySeqDesc(Long sessionId, List<ChatMessageStatus> statuses);

    Optional<ChatMessageEntity> findBySessionIdAndClientMsgIdAndRole(Long sessionId, UUID clientMsgId, ChatRole role);

    boolean existsBySessionIdAndStatus(Long sessionId, ChatMessageStatus status);

    // 서버 재시작 등으로 STREAMING 에 멈춘 행을 정리
    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.status = com.example.maum.chat.ChatMessageStatus.INTERRUPTED "
            + "WHERE m.sessionId = :sessionId AND m.status = com.example.maum.chat.ChatMessageStatus.STREAMING "
            + "AND m.createdAt < :before")
    int interruptStale(@Param("sessionId") Long sessionId, @Param("before") Instant before);
}
