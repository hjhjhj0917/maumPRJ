package com.example.maum.repository.projection;

import java.math.BigDecimal;

public interface DepressionTrendProjection {

    String getMonth();

    BigDecimal getAvgDepScore();

    Long getDiaryCount();
}
