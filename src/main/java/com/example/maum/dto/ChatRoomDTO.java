package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChatRoomDTO(
        Integer chatRoomNo,
        String userNo,
        String roomTitle,
        Integer isPinned,
        String createdAt,
        String updatedAt
) {
}
