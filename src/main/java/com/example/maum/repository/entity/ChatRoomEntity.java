package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CHAT_ROOM")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class ChatRoomEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHAT_ROOM_NO")
    private Integer chatRoomNo;

    @Column(name = "USER_NO", nullable = false)
    private String userNo;

    @Column(name = "ROOM_TITLE", length = 100)
    private String roomTitle;

    @Column(name = "IS_PINNED", nullable = false)
    private Integer isPinned;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

}
