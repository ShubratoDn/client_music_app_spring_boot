package com.music.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.music.app.entity.Track;
import com.music.app.service.TrackService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class MusicController {

    @Autowired
    private TrackService trackService;

    @GetMapping("/player/music/{id}")
    public String musicPlay(@PathVariable Long id, HttpServletRequest request, Model model){

        Track track = trackService.getTrackById(id);

        List<Map<String, String>> playlist = List.of(
                Map.of("title", track.getName(), "file", track.getAudioUrl())
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            String playlistJson = mapper.writeValueAsString(playlist);
            model.addAttribute("playlistJson", playlistJson);
        } catch (Exception e) {
            model.addAttribute("playlistJson", "[]");
        }

        return "player";
    }

    @GetMapping("/player/playlist/{id}")
    public String musicPlayerView(@PathVariable Long id, Model model) {
        List<Map<String, String>> playlist = List.of(
                Map.of("title", "No Music", "file", "03c2b5fa-d6f8-4b05-970b-8678b6b2fdd7-2-MB-OGG.ogg"),
                Map.of("title", "Test 123", "file", "80s_vibe"),
                Map.of("title", "Test 456", "file", "running_out"),
                Map.of("title", "Test 789", "file", "running_out")
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            String playlistJson = mapper.writeValueAsString(playlist);
            model.addAttribute("playlistJson", playlistJson);
        } catch (Exception e) {
            model.addAttribute("playlistJson", "[]"); // Fallback in case of error
        }

        return "player";
    }



    @GetMapping("/player/album/{id}")
    public String musicAlbum(@PathVariable Long id, Model model) {
        List<Track> tracksByAlbum = trackService.getTracksByAlbumId(id);
        // Map the tracks to the required playlist format
        List<Map<String, String>> playlist = tracksByAlbum.stream()
                .map(track -> Map.of(
                        "title", track.getName(),
                        "file", track.getAudioUrl()
                ))
                .toList();

        try {
            ObjectMapper mapper = new ObjectMapper();
            String playlistJson = mapper.writeValueAsString(playlist);
            model.addAttribute("playlistJson", playlistJson);
        } catch (Exception e) {
            model.addAttribute("playlistJson", "[]"); // Fallback in case of error
        }

        return "player";
    }


    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
