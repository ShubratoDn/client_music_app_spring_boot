package com.music.app.controller;

import com.music.app.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/spotify")
public class SpotifyAuthController {

    @Autowired
    private SpotifyService spotifyService;


    @GetMapping("/connect")
    public String connectToSpotify() {
        return "redirect:" + spotifyService.getAuthorizationUrl();
    }


    @GetMapping("/callback")
    public String handleSpotifyCallback(@RequestParam("code") String code, Model model) {
        String accessToken = spotifyService.exchangeCodeForToken(code);
        System.out.println("\n\n\nAccess token: " + accessToken);
        if (accessToken != null) {
            spotifyService.setAccessToken(accessToken);
            spotifyService.fetchAndStoreUserInfo();
        }
        return "redirect:/";
    }

    @GetMapping("/disconnect")
    public String disconnectFromSpotify() {
        spotifyService.clearSpotifySession();
        return "redirect:/";
    }

    @GetMapping("/recently-played")
    public String showRecentlyPlayed(Model model) {
        model.addAttribute("recentlyPlayedTracks", spotifyService.getRecentlyPlayedTracks());
        return "spotify-recently-played"; // View for recently played tracks
    }


    @GetMapping("/playlists")
    public String getUserPlaylists(Model model) {
        List<Map<String, Object>> playlists = spotifyService.getUserPlaylists();
        model.addAttribute("playlists", playlists);
        return "spotify-playlists";
    }


}
