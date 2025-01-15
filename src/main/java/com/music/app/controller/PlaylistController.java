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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


    @GetMapping("/my-playlist")
    public String showUserPlaylists(Model model ) {
        // Get the logged-in user
        User user = userService.getLoggedInUser();

        // Fetch playlists for the user
        List<Playlist> playlists = playlistService.getPlaylistsByUser(user);

        // Add playlists to the model
        model.addAttribute("playlists", playlists);

        return "my-playlist";
    }


    @DeleteMapping("/api/playlist/remove-track/{trackId}")
    public ResponseEntity<String> removeTrackFromPlaylist(@PathVariable Long trackId, @RequestParam Long playlistId) {
        try {
            playlistService.removeTrackFromPlaylist(playlistId, trackId);
            return ResponseEntity.ok("Track removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing track: " + e.getMessage());
        }
    }

    @GetMapping("/playlist/remove-playlist/{playlistId}")
    public String removePlaylist(@PathVariable Long playlistId, RedirectAttributes redirectAttributes) {
        try {
            playlistService.removePlaylist(playlistId);
            redirectAttributes.addFlashAttribute("success", "Playlist removed successfully");
            return "redirect:/my-playlist";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error removing playlist: " + e.getMessage());
            return "redirect:/my-playlist";
        }
    }
}
