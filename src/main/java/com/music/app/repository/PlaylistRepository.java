package com.music.app.repository;

import com.music.app.entity.Playlist;
import com.music.app.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
    List<Playlist> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    void deleteByTracks_Id(Long trackId);

    List<Playlist> findByTracks_Id(Long trackId);
}
