package com.music.app.repository;

import com.music.app.entity.Album;
import com.music.app.entity.Playlist;
import com.music.app.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    public List<Track> findByAlbum(Album album);
    public List<Track> findByPlaylists(Playlist playlist);

    List<Track> findByNameContainingIgnoreCase(String query);

    List<Track> findByNameContainingIgnoreCaseOrAlbum_NameContainingIgnoreCase(String name, String albumName);
}
