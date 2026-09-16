package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DiaryStatsDTO(
        Long totalCount,
        Integer currentStreak,
        Integer longestStreak
) {
}
