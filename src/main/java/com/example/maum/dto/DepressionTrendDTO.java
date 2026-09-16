package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DepressionTrendDTO(
        String month,
        BigDecimal avgDepScore,
        Long diaryCount
) {
}
