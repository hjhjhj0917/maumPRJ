package com.example.maum.repository;

import com.example.maum.repository.entity.DiaryMusicEntity;
import com.example.maum.repository.projection.TopMusicProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// ★ 즐겨찾기 이후 추가/수정
public interface DiaryMusicRepository extends JpaRepository<DiaryMusicEntity, Integer> {

    List<DiaryMusicEntity> findByDiaryNoOrderByTrackOrderAsc(Integer diaryNo);

    void deleteByDiaryNo(Integer diaryNo);

    // 마이페이지 - 사용자에게 가장 많이 추천된 곡 Top N (동일 곡+아티스트 기준 집계)
    @Query(value = "SELECT m.TRACK_NAME AS trackName, m.ARTIST_NAME AS artistName, " +
            "MAX(m.ALBUM_IMAGE_URL) AS albumImageUrl, MAX(m.SPOTIFY_URL) AS spotifyUrl, " +
            "COUNT(*) AS recommendCount " +
            "FROM DIARY_MUSIC m JOIN DIARY d ON m.DIARY_NO = d.DIARY_NO " +
            "WHERE d.USER_NO = :userNo " +
            "GROUP BY m.TRACK_NAME, m.ARTIST_NAME " +
            "ORDER BY recommendCount DESC, MAX(m.CREATED_AT) DESC",
            nativeQuery = true)
    List<TopMusicProjection> findTopRecommendedMusicByUserNo(@Param("userNo") String userNo, Pageable pageable);
}
