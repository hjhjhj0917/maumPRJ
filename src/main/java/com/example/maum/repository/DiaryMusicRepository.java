package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryMusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryMusicRepository extends JpaRepository<DiaryMusicEntity, Long> {

    List<DiaryMusicEntity> findByDiaryIdOrderByTrackOrderAsc(Long diaryId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM DiaryMusicEntity m WHERE m.diaryId = :diaryId")
    int deleteByDiaryId(@Param("diaryId") Long diaryId);
}
