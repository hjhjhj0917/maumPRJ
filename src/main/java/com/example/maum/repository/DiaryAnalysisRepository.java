package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryAnalysisEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DiaryAnalysisRepository extends JpaRepository<DiaryAnalysisEntity, Long> {

    // 아직 끝나지 않았거나 실패한 단계가 있는 건. from~to 구간만 보므로 너무 오래된 건(포기)과 방금 시작한 건(진행 중)은 제외됨
    @Query("SELECT a.diaryId FROM DiaryAnalysisEntity a WHERE a.analyzedAt BETWEEN :from AND :to AND ("
            + "a.analysisStatus IN (com.example.maum.diary.AnalysisStatus.PENDING, com.example.maum.diary.AnalysisStatus.FAILED) "
            + "OR a.summaryStatus IN (com.example.maum.diary.StageStatus.PENDING, com.example.maum.diary.StageStatus.FAILED) "
            + "OR a.embeddingStatus IN (com.example.maum.diary.StageStatus.PENDING, com.example.maum.diary.StageStatus.FAILED)) "
            + "ORDER BY a.analyzedAt")
    List<Long> findRetryTargets(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}
