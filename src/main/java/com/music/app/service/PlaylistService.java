package com.music.app.service;

import com.music.app.entity.Playlist;
import com.music.app.entity.Track;
import com.music.app.entity.User;
import com.music.app.repository.PlaylistRepository;
import com.music.app.repository.TrackRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private TrackRepository trackRepository;

    public Playlist getPlaylistById(Long id){
        return playlistRepository.findById(id).orElse(null);
    }

    public Playlist createNewPlaylist(String playlistName, User user) {
        if (playlistName == null || playlistName.isBlank()) {
            throw new IllegalArgumentException("Playlist name cannot be empty.");
        }

        Playlist playlist = new Playlist();
        playlist.setName(playlistName);
        playlist.setUser(user);

        return playlistRepository.save(playlist);
    }


    public void addTrackToPlaylist(Playlist playlist, Track track) {
        if (playlist == null || track == null) {
            throw new IllegalArgumentException("Playlist or track cannot be null.");
        }

        // Check if the track is already in the playlist
        if (!playlist.getTracks().contains(track)) {
            playlist.getTracks().add(track);
            playlistRepository.save(playlist);
        } else {
            throw new IllegalArgumentException("Track is already in the playlist.");
        }
    }

    public List<Playlist> getPlaylistsByUser(User user) {
        return playlistRepository.findByUser(user);
    }


    public void removeTrackFromPlaylist(Long playlistId, Long trackId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        if (playlist.getTracks().contains(track)) {
            playlist.getTracks().remove(track);
            playlistRepository.save(playlist);
        } else {
            throw new IllegalArgumentException("Track is not in the playlist");
        }
    }


    public void removePlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        playlistRepository.delete(playlist);
    }

    public List<Playlist> searchPlaylists(String query) {
        return playlistRepository.findByNameContainingIgnoreCase(query);
    }
}
