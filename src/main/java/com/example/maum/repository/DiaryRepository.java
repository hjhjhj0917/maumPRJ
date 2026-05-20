package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Integer> {

    List<DiaryEntity> findAllByUserNoAndCreatedAtBetween(String userNo, LocalDate startDate, LocalDate endDate);

    List<DiaryEntity> findByUserNoAndTitleContainingOrderByCreatedAtDesc(String userNo, String title);

    List<DiaryEntity> findByUserNoAndEmotionColorInOrderByCreatedAtDesc(String userNo, List<String> colors);

    List<DiaryEntity> findTop10ByUserNoOrderByCreatedAtDesc(String userNo);
}
