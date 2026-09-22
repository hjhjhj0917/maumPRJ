package com.example.maum.repository.projection;

// ★ 즐겨찾기 이후 추가/수정
public interface TopMusicProjection {

    String getTrackName();

    String getArtistName();

    String getAlbumImageUrl();

    String getSpotifyUrl();

    Long getRecommendCount();
}
