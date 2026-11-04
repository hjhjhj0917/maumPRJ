package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "diary_music")
@DynamicInsert
@Builder
@Entity
public class DiaryMusicEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "diary_id", nullable = false)
    private Long diaryId;

    @Column(name = "track_order", nullable = false)
    private Short trackOrder;

    @Column(name = "track_id", nullable = false, length = 64)
    private String trackId;

    @Column(name = "track_name", nullable = false, length = 200)
    private String trackName;

    @Column(name = "artist_name", nullable = false, length = 200)
    private String artistName;

    @Column(name = "album_image_url", length = 500)
    private String albumImageUrl;

    @Column(name = "spotify_url", nullable = false, length = 300)
    private String spotifyUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
