package com.music.app.controller;

import com.music.app.entity.Playlist;
import com.music.app.entity.Track;
import com.music.app.entity.User;
import com.music.app.service.PlaylistService;
import com.music.app.service.TrackService;
import com.music.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private TrackService trackService;

    @PostMapping("/playlists/addTrack")
    @ResponseBody
    public ResponseEntity<?> addTrackToPlaylist(
            @RequestParam Long trackId,
            @RequestParam(required = false) Long playlistId,
            @RequestParam(required = false) String newPlaylistName) {
        try {
            Playlist playlist;

            if (playlistId != null && playlistId > 0) {
                // Add to existing playlist
                playlist = playlistService.getPlaylistById(playlistId);
            } else if (newPlaylistName != null && !newPlaylistName.isBlank()) {
                // Create new playlist
                User loggedInUser = userService.getLoggedInUser();
                playlist = playlistService.createNewPlaylist(newPlaylistName, loggedInUser);
            } else {
                return ResponseEntity.badRequest().body("Invalid playlist data.");
            }

            // Add track to the playlist
            Track track = trackService.getTrackById(trackId);
            playlistService.addTrackToPlaylist(playlist, track);

            return ResponseEntity.ok("Track added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding track to playlist.");
        }
    }



    @GetMapping("/api/user/playlists")
    @ResponseBody
    public ResponseEntity<List<Playlist>> getUserPlaylists() {
        // Get the logged-in user from the authentication object
        User user = userService.getLoggedInUser();

        // Fetch playlists for the user
        List<Playlist> playlists = playlistService.getPlaylistsByUser(user);

        return ResponseEntity.ok(playlists);
    }

}
