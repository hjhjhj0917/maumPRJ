package com.example.maum.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record WeatherDTO(
        Double lat,
        Double lon,
        String weatherEmoji,
        String weatherText,
        Double maxTemp,
        Double minTemp
) {
}
