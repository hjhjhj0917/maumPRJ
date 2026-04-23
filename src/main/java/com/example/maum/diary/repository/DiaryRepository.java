package com.example.maum.diary.repository;

import com.example.maum.diary.repository.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Integer> {

    List<DiaryEntity> findAllByUserNoAndCreatedAtBetween(Integer userNo, LocalDate startDate, LocalDate endDate);
}
