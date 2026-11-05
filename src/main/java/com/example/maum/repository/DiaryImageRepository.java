package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImageEntity, Long> {

    List<DiaryImageEntity> findByDiaryIdOrderBySortOrderAsc(Long diaryId);

    int countByDiaryId(Long diaryId);

    // 탈퇴 정리 때 지울 스토리지 파일 목록
    @Query("SELECT i.storageKey FROM DiaryImageEntity i JOIN DiaryEntity d ON d.id = i.diaryId WHERE d.userId IN :userIds")
    List<String> findStorageKeysByUserIds(@Param("userIds") List<Long> userIds);
}
