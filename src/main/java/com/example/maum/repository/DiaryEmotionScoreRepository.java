package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryEmotionScoreEntity;
import com.example.maum.repository.entity.DiaryEmotionScoreId;
import com.example.maum.repository.projection.EmotionScoreProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryEmotionScoreRepository extends JpaRepository<DiaryEmotionScoreEntity, DiaryEmotionScoreId> {

    // 점수 높은 순(같으면 감정 번호 순). 대표 감정은 이 결과의 첫 행, 상위 10개는 pageable 로 자름
    @Query("SELECT s.id.emotionId AS emotionId, s.score AS score FROM DiaryEmotionScoreEntity s "
            + "WHERE s.id.diaryId = :diaryId ORDER BY s.score DESC, s.id.emotionId ASC")
    List<EmotionScoreProjection> findTop(@Param("diaryId") Long diaryId, Pageable pageable);

    // 재분석 때 이전 점수를 한 번에 지움 (행 단위 삭제로 44번 쿼리가 나가지 않게 함)
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM DiaryEmotionScoreEntity s WHERE s.id.diaryId = :diaryId")
    int deleteByDiaryId(@Param("diaryId") Long diaryId);
}
