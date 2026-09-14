package com.example.maum.repository;

import com.example.maum.repository.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByChatRoomNoOrderByCreatedAtAsc(Integer chatRoomNo);

    // 최근 대화 맥락을 Gemini에 넘기기 위한 최근 8개(4턴) 조회 — 최신순으로 오므로 서비스단에서 다시 뒤집어서 씀
    List<ChatMessageEntity> findTop8ByChatRoomNoOrderByCreatedAtDesc(Integer chatRoomNo);
}
