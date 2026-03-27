package com.example.maum.diary.repository;

import com.example.maum.diary.repository.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Integer> {
}
