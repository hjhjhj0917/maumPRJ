package com.example.maum.repository.projection;

public interface TopMusicProjection {

    String getTrackName();

    String getArtistName();

    String getAlbumImageUrl();

    String getSpotifyUrl();

    Long getRecommendCount();
}
