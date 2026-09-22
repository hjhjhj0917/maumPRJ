package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// ★ 즐겨찾기 이후 추가/수정
public interface DiaryImageRepository extends JpaRepository<DiaryImageEntity, Integer> {

    List<DiaryImageEntity> findByDiaryNoOrderByImageOrderAsc(Integer diaryNo);

    int countByDiaryNo(Integer diaryNo);
}
