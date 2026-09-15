package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryMusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryMusicRepository extends JpaRepository<DiaryMusicEntity, Integer> {

    List<DiaryMusicEntity> findByDiaryNoOrderByTrackOrderAsc(Integer diaryNo);

    void deleteByDiaryNo(Integer diaryNo);
}
