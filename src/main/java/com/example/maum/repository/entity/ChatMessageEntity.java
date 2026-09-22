package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CHAT_MESSAGE")
@DynamicInsert
@Builder
@Entity
// ★ 즐겨찾기 이후 추가/수정
public class ChatMessageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_MSG_NO")
    private Long chatMsgNo;

    @Column(name = "CHAT_ROOM_NO", nullable = false)
    private Integer chatRoomNo;

    @Column(name = "ROLE", nullable = false, length = 10)
    private String role;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "HAS_AUDIO", nullable = false)
    private Boolean hasAudio;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

}
