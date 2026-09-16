package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ReportCardDTO(
        String comment,
        String periodStart,
        String periodEnd,
        Integer diaryCount,
        BigDecimal avgDepScore
) {
}
