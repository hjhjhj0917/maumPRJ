package com.example.maum.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DiaryEmotionScoreId implements Serializable {

    @Column(name = "diary_id")
    private Long diaryId;

    @Column(name = "emotion_id")
    private Short emotionId;
}
