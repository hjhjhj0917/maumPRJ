package com.example.maum.repository;

import com.example.maum.repository.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

    List<ChatRoomEntity> findByUserNoOrderByIsPinnedDescUpdatedAtDesc(String userNo);

    Optional<ChatRoomEntity> findByChatRoomNoAndUserNo(Integer chatRoomNo, String userNo);

    long deleteByChatRoomNoAndUserNo(Integer chatRoomNo, String userNo);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE CHAT_ROOM SET UPDATED_AT = :updatedAt WHERE CHAT_ROOM_NO = :chatRoomNo",
            nativeQuery = true)
    int touchUpdatedAt(@Param("chatRoomNo") Integer chatRoomNo, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE CHAT_ROOM SET ROOM_TITLE = :roomTitle, UPDATED_AT = :updatedAt WHERE CHAT_ROOM_NO = :chatRoomNo",
            nativeQuery = true)
    int updateTitleAndTouch(@Param("chatRoomNo") Integer chatRoomNo, @Param("roomTitle") String roomTitle, @Param("updatedAt") LocalDateTime updatedAt);

    // 사이드바 인라인 이름변경 — 위 자동 제목 설정과 달리 UPDATED_AT(최근순 정렬 기준)은 건드리지 않음
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE CHAT_ROOM SET ROOM_TITLE = :roomTitle WHERE CHAT_ROOM_NO = :chatRoomNo AND USER_NO = :userNo",
            nativeQuery = true)
    int updateTitleDirectly(@Param("chatRoomNo") Integer chatRoomNo, @Param("userNo") String userNo, @Param("roomTitle") String roomTitle);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE CHAT_ROOM SET IS_PINNED = :isPinned WHERE CHAT_ROOM_NO = :chatRoomNo AND USER_NO = :userNo",
            nativeQuery = true)
    int updatePinnedDirectly(@Param("chatRoomNo") Integer chatRoomNo, @Param("userNo") String userNo, @Param("isPinned") Integer isPinned);
}
