package com.example.maum.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;

@Document(collection = "MENTAL_INST")
public record MentalInstDTO(
        @Id
        String id,

        @Field("NAME")
        String name,

        @Field("ADDR")
        String addr,

        @Field("LOCATION")
        Location location
) {
    public record Location(
            String type,
            List<Double> coordinates
    ) {}
}