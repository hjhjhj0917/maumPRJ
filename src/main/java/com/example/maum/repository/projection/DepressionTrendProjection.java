package com.example.maum.repository.projection;

import java.math.BigDecimal;

// ★ 즐겨찾기 이후 추가/수정
public interface DepressionTrendProjection {

    String getMonth();

    BigDecimal getAvgDepScore();

    Long getDiaryCount();
}
