package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// 감정 대분류 9종(읽기 전용 시드). 색상은 대분류에만 있다
@Getter
@NoArgsConstructor
@Table(name = "emotion_group")
@Entity
public class EmotionGroupEntity implements Serializable {

    @Id
    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 20)
    private String nameKo;

    @Column(name = "color", nullable = false, length = 7)
    private String color;
}
