package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ChatRoomDTO(
        String publicId,
        String userPublicId,
        String roomTitle,
        Boolean isPinned,
        String createdAt,
        String updatedAt
) {
}
