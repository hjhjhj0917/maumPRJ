package com.example.maum.repository.projection;

import java.time.LocalDate;
import java.util.UUID;

// 목록·달력에 필요한 컬럼만 읽는 조회 결과 (본문은 읽지 않음). mainGroupCode 는 분석 전이면 null
public interface DiaryListProjection {

    UUID getPublicId();

    String getTitle();

    LocalDate getDiaryDate();

    Boolean getFavorite();

    Boolean getPinned();

    String getMainGroupCode();
}
