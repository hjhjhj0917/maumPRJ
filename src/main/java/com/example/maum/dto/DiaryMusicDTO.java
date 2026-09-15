package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record DiaryMusicDTO(
        Integer musicNo,
        Integer diaryNo,
        String trackId,
        String trackName,
        String artistName,
        String albumImageUrl,
        String spotifyUrl,
        Integer trackOrder
) {
}
