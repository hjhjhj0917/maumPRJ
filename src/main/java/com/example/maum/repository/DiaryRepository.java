package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Integer> {

    List<DiaryEntity> findAllByUserNoAndCreatedAtBetween(String userNo, LocalDate startDate, LocalDate endDate);

    List<DiaryEntity> findByUserNoAndTitleContainingOrderByCreatedAtDesc(String userNo, String title);

    List<DiaryEntity> findByUserNoAndEmotionColorInOrderByCreatedAtDesc(String userNo, List<String> colors);

    List<DiaryEntity> findTop20ByUserNoOrderByCreatedAtDesc(String userNo);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE DIARY SET SUMMARY = :summary, MAIN_EMOTION = :mainEmotion, " +
            "EMOTION_COLOR = :emotionColor, DEP_LVL = :depLvl, DEP_SCORE = :depScore, SYMPTOM_YN = :symptomYn " +
            "WHERE DIARY_NO = :diaryNo",
            nativeQuery = true)
    int updateAnalysisResultDirectly(
            @Param("diaryNo") Long diaryNo,
            @Param("summary") String summary,
            @Param("mainEmotion") String mainEmotion,
            @Param("emotionColor") String emotionColor,
            @Param("depLvl") Integer depLvl,
            @Param("depScore") BigDecimal depScore,
            @Param("symptomYn") Integer symptomYn
    );

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE DIARY SET TITLE = :title, CONTENT = :content WHERE DIARY_NO = :diaryNo",
            nativeQuery = true)
    int updateDiaryDirectly(
            @Param("diaryNo") Long diaryNo,
            @Param("title") String title,
            @Param("content") String content
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiaryEntity d SET d.isFavorite = :isFavorite WHERE d.diaryNo = :diaryNo AND d.userNo = :userNo")
    int updateFavorite(
            @Param("diaryNo") Integer diaryNo,
            @Param("userNo") String userNo,
            @Param("isFavorite") Integer isFavorite
    );
}
