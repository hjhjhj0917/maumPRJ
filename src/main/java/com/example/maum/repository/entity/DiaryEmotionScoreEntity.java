package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

// 일기 1건당 감정 44개의 점수(KOTE 는 감정별 독립 확률이라 합이 1이 아님).
// 키를 직접 채우는 엔티티는 save() 가 행마다 SELECT 로 존재 여부를 먼저 확인하는데, 이 행들은 항상 새로 넣는 것이라
// Persistable 로 "새 행"임을 알려 44번의 불필요한 조회를 막는다.
@Getter
@NoArgsConstructor
@Table(name = "diary_emotion_score")
@Entity
public class DiaryEmotionScoreEntity implements Persistable<DiaryEmotionScoreId> {

    @EmbeddedId
    private DiaryEmotionScoreId id;

    @Column(name = "score", nullable = false)
    private float score;

    public DiaryEmotionScoreEntity(Long diaryId, Short emotionId, float score) {
        this.id = new DiaryEmotionScoreId(diaryId, emotionId);
        this.score = score;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
