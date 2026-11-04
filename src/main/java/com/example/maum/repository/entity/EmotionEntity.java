package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// 세부 감정 44종(읽기 전용 시드). 모델이 돌려주는 라벨(nameKo)을 점수 테이블 키(id)로 바꾸는 데 쓴다
@Getter
@NoArgsConstructor
@Table(name = "emotion")
@Entity
public class EmotionEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 30)
    private String nameKo;

    @Column(name = "group_code", nullable = false, length = 20)
    private String groupCode;
}
