package com.music.app.repository;

import com.music.app.entity.Album;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByNameContainingIgnoreCaseOrArtistContainingIgnoreCase(String name, String artist);
    @Modifying
    @Transactional
    @Query("DELETE FROM Track t WHERE t.id = :trackId AND t.album.id = :albumId")
    void deleteTrackFromAlbum(@Param("trackId") Long trackId, @Param("albumId") Long albumId);
}