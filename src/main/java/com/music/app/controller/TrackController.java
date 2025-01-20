package com.music.app.controller;

import com.music.app.entity.Track;
import com.music.app.service.AlbumService;
import com.music.app.service.PlaylistService;
import com.music.app.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;
    @Autowired
    private AlbumService albumService;
    @Autowired
    private PlaylistService playlistService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/upload")
    public String showAddTrackForm(Model model) {
        model.addAttribute("albums", albumService.getAllAlbums());
        return "add-track";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/upload")
    public String uploadTrack(
            @RequestParam("name") String name,
            @RequestParam(value = "duration", required = false) String duration,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam("audioFile") MultipartFile audioFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        StringBuilder errors = new StringBuilder();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.append("Track name cannot be empty. ");
        }

        // Validate audio file
        if (audioFile == null || audioFile.isEmpty()) {
            errors.append("Audio file is required. ");
        } else {
            // Validate file size (10 KB - 30 MB)
            long fileSize = audioFile.getSize();
            if (fileSize < 10 * 1024) {
                errors.append("Audio file size is too small (minimum is 10 KB). ");
            } else if (fileSize > 30 * 1024 * 1024) {
                errors.append("Audio file size exceeds the maximum limit of 30 MB. ");
            }

            // Validate file type (only audio)
            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                errors.append("Invalid file type. Only audio files are allowed. ");
            }
        }


        // If there are errors, set them in the model and return the upload view
        if (errors.length() > 0) {
            model.addAttribute("error", errors.toString());
            return "add-track"; // Render the upload page with error message
        }

        // No errors, proceed with track saving
        try {
            trackService.saveTrack(name, duration, albumId, audioFile);
            redirectAttributes.addFlashAttribute("success", "Track uploaded successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to upload track: " + e.getMessage());
            return "add-track";
        }

        return "redirect:/tracks/upload";
    }


    @GetMapping
    public String getAllTracks(Model model) {
        List<Track> tracks = trackService.getAllTracks();
        model.addAttribute("tracks", tracks);
        return "tracks-list";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/edit/{id}")
    public String showUpdateTrackForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Track track = trackService.getTrackById(id);
        if (track == null) {
            redirectAttributes.addFlashAttribute("error", "Track not found!");
            return "redirect:/tracks"; // Redirect to the track list if the track doesn't exist
        }

        model.addAttribute("track", track);
        model.addAttribute("albums", albumService.getAllAlbums()); // Include albums for dropdown
        return "update-track";
    }



    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/update/{id}")
    public String updateTrack(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "duration", required = false) String duration,
            @RequestParam(value = "albumId", required = false) Long albumId,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        StringBuilder errors = new StringBuilder();

        // Validate name
        if (name == null || name.trim().isEmpty()) {
            errors.append("Track name cannot be empty. ");
        }

        // Validate audio file if provided
        if (audioFile != null && !audioFile.isEmpty()) {
            long fileSize = audioFile.getSize();
            if (fileSize < 10 * 1024) {
                errors.append("Audio file size is too small (minimum is 10 KB). ");
            } else if (fileSize > 30 * 1024 * 1024) {
                errors.append("Audio file size exceeds the maximum limit of 30 MB. ");
            }

            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                errors.append("Invalid file type. Only audio files are allowed. ");
            }
        }

        // If errors exist, return to the update view
        if (errors.length() > 0) {
            model.addAttribute("error", errors.toString());
            model.addAttribute("track", trackService.getTrackById(id));
            model.addAttribute("albums", albumService.getAllAlbums());
            return "update-track";
        }

        try {
            trackService.updateTrack(id, name, duration, albumId, audioFile);
            redirectAttributes.addFlashAttribute("success", "Track updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update track: " + e.getMessage());
            return "redirect:/tracks/edit/" + id;
        }

        return "redirect:/tracks";
    }



    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<Track>> searchTracks(@RequestParam String q) {
        System.out.println("Searching for '" + q+"'");
        List<Track> tracks;
        if(q == null || q.isBlank()){
            tracks = trackService.getAllTracks();
        }else{
            tracks = trackService.searchTracks(q);
        }
        System.out.println(" And size is "+ tracks.size());
        for (Track track : tracks){
            System.out.println(track);
        }
        System.out.println("====================");
        return ResponseEntity.ok(tracks);
    }



    @GetMapping("/search-track")
    public String searchTrack() {
        return "search-track";
    }

    @GetMapping("/search2")
    @ResponseBody
    public Map<String, List<?>> searchEntities(@RequestParam String query) {
        Map<String, List<?>> results = new HashMap<>();
        results.put("tracks", trackService.searchTracks2(query));
        results.put("albums", albumService.searchAlbums(query));
        results.put("playlists", playlistService.searchPlaylists(query));
        return results;
    }

}
