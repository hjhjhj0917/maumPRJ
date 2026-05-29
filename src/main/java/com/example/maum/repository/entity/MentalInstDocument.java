package com.example.maum.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "MENTAL_INST")
public class MentalInstDocument {

    @Id
    private String id;

    @Field("NAME")
    private String name;

    @Field("ADDR")
    private String addr;

    @Field("LOCATION")
    private Location location;

    @Field("CATEGORY")
    private String category;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String type;
        private List<Double> coordinates;
    }
}