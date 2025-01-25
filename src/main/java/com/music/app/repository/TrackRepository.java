package com.music.app.repository;

import com.music.app.entity.Album;
import com.music.app.entity.Playlist;
import com.music.app.entity.Track;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    public List<Track> findByAlbum(Album album);
    public List<Track> findByPlaylists(Playlist playlist);

    List<Track> findByNameContainingIgnoreCase(String query);

    List<Track> findByNameContainingIgnoreCaseOrAlbum_NameContainingIgnoreCase(String name, String albumName);

    @Modifying
    @Transactional
    @Query("UPDATE Track t SET t.album = NULL WHERE t.album.id = :albumId")
    void removeTracksFromAlbum(@Param("albumId") Long albumId);
}
