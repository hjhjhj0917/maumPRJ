package com.example.maum.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DIARY_MUSIC")
@DynamicInsert
@Builder
@Entity
public class DiaryMusicEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MUSIC_NO")
    private Integer musicNo;

    @Column(name = "DIARY_NO", nullable = false)
    private Integer diaryNo;

    @Column(name = "TRACK_ID", nullable = false, length = 50)
    private String trackId;

    @Column(name = "TRACK_NAME", nullable = false, length = 200)
    private String trackName;

    @Column(name = "ARTIST_NAME", nullable = false, length = 200)
    private String artistName;

    @Column(name = "ALBUM_IMAGE_URL", length = 500)
    private String albumImageUrl;

    @Column(name = "SPOTIFY_URL", nullable = false, length = 300)
    private String spotifyUrl;

    @Column(name = "TRACK_ORDER", nullable = false)
    private Integer trackOrder;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
