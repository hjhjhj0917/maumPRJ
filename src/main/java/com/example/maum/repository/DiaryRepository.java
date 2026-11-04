package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.projection.DiaryListProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

    // 목록용 공통 SELECT. 분석 결과는 대분류 코드(색상 표시용)만 필요해서 LEFT JOIN 으로 함께 읽음
    String LIST_FROM = "SELECT d.publicId AS publicId, d.title AS title, d.diaryDate AS diaryDate, "
            + "d.favorite AS favorite, d.pinned AS pinned, a.mainGroupCode AS mainGroupCode "
            + "FROM DiaryEntity d LEFT JOIN DiaryAnalysisEntity a ON a.diaryId = d.id "
            + "WHERE d.userId = :userId AND d.status = com.example.maum.diary.DiaryStatus.";
    String PUBLISHED = LIST_FROM + "PUBLISHED ";

    // 소유자 조건을 쿼리에 넣어 다른 사람의 일기를 아예 읽지 못하게 함
    Optional<DiaryEntity> findByPublicIdAndUserId(UUID publicId, Long userId);

    @Query(PUBLISHED + "AND d.diaryDate BETWEEN :start AND :end ORDER BY d.diaryDate, d.id")
    List<DiaryListProjection> findMonthly(@Param("userId") Long userId,
                                          @Param("start") LocalDate start, @Param("end") LocalDate end);

    // keyword 의 % _ 는 호출하는 쪽에서 \ 로 이스케이프해서 넘김
    @Query(PUBLISHED + "AND LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ESCAPE '\\' "
            + "ORDER BY d.diaryDate DESC, d.id DESC")
    List<DiaryListProjection> searchByTitle(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query(PUBLISHED + "AND a.mainGroupCode IN :groupCodes ORDER BY d.diaryDate DESC, d.id DESC")
    List<DiaryListProjection> findByGroupCodes(@Param("userId") Long userId,
                                               @Param("groupCodes") Collection<String> groupCodes);

    @Query(PUBLISHED + "ORDER BY d.pinned DESC, d.diaryDate DESC, d.id DESC")
    List<DiaryListProjection> findRecent(@Param("userId") Long userId, Pageable pageable);

    @Query(PUBLISHED + "AND d.favorite = TRUE ORDER BY d.diaryDate DESC, d.id DESC")
    List<DiaryListProjection> findFavorites(@Param("userId") Long userId);

    @Query(LIST_FROM + "DRAFT ORDER BY d.updatedAt DESC, d.id DESC")
    List<DiaryListProjection> findDrafts(@Param("userId") Long userId);
}
