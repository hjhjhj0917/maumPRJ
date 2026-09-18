package com.example.maum.dto;

import com.example.maum.repository.entity.MentalInstDocument;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record MentalInstDTO(
        String id,
        String name,
        String addr,
        Location location,
        String category
) {

    @Builder
    public record Location(
            String type,
            java.util.List<Double> coordinates
    ) {
        public static Location from(MentalInstDocument.Location location) {
            if (location == null) return null;

            return Location.builder()
                    .type(location.getType())
                    .coordinates(location.getCoordinates())
                    .build();
        }
    }

    public static MentalInstDTO from(MentalInstDocument entity) {

        return MentalInstDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .addr(entity.getAddr())
                .location(Location.from(entity.getLocation()))
                .category(entity.getCategory())
                .build();
    }
}
