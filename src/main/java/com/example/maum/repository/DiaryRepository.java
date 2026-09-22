package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.projection.DepressionTrendProjection;
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

    // ★ 즐겨찾기 이후 추가/수정
    List<DiaryEntity> findTop20ByUserNoOrderByIsPinnedDescCreatedAtDesc(String userNo);

    // ★ 즐겨찾기 이후 추가/수정
    List<DiaryEntity> findByUserNoAndIsFavoriteOrderByCreatedAtDesc(String userNo, Integer isFavorite);

    // ★ 즐겨찾기 이후 추가/수정
    long countByUserNo(String userNo);

    // ★ 즐겨찾기 이후 추가/수정
    // 연속 작성일 계산용 - 최신순으로 작성 날짜만 조회
    @Query("SELECT DISTINCT d.createdAt FROM DiaryEntity d WHERE d.userNo = :userNo ORDER BY d.createdAt DESC")
    List<LocalDate> findAllCreatedAtByUserNoOrderByCreatedAtDesc(@Param("userNo") String userNo);

    // ★ 즐겨찾기 이후 추가/수정
    // 마이페이지 - 최근 N개월 월별 평균 우울 지수 추이
    @Query(value = "SELECT DATE_FORMAT(CREATED_AT, '%Y-%m') AS month, " +
            "AVG(DEP_SCORE) AS avgDepScore, COUNT(*) AS diaryCount " +
            "FROM DIARY " +
            "WHERE USER_NO = :userNo AND DEP_SCORE IS NOT NULL AND CREATED_AT >= :fromDate " +
            "GROUP BY DATE_FORMAT(CREATED_AT, '%Y-%m') " +
            "ORDER BY month ASC",
            nativeQuery = true)
    List<DepressionTrendProjection> findDepressionTrendByUserNo(
            @Param("userNo") String userNo,
            @Param("fromDate") LocalDate fromDate
    );

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

    // ★ 즐겨찾기 이후 추가/수정
    @Modifying(clearAutomatically = true)
    @Query("UPDATE DiaryEntity d SET d.isFavorite = :isFavorite WHERE d.diaryNo = :diaryNo AND d.userNo = :userNo")
    int updateFavorite(
            @Param("diaryNo") Integer diaryNo,
            @Param("userNo") String userNo,
            @Param("isFavorite") Integer isFavorite
    );

    // ★ 즐겨찾기 이후 추가/수정
    // 제목만 바꾸는 경우 diaryUpdate(전체 수정)처럼 CONTENT까지 다시 보낼 필요 없게, 제목만 직접 수정
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE DIARY SET TITLE = :title WHERE DIARY_NO = :diaryNo AND USER_NO = :userNo",
            nativeQuery = true)
    int updateTitleDirectly(
            @Param("diaryNo") Integer diaryNo,
            @Param("userNo") String userNo,
            @Param("title") String title
    );

    // ★ 즐겨찾기 이후 추가/수정
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE DIARY SET IS_PINNED = :isPinned WHERE DIARY_NO = :diaryNo AND USER_NO = :userNo",
            nativeQuery = true)
    int updatePinnedDirectly(
            @Param("diaryNo") Integer diaryNo,
            @Param("userNo") String userNo,
            @Param("isPinned") Integer isPinned
    );
}
