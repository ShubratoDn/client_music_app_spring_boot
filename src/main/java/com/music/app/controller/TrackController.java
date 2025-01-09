package com.music.app.controller;

import com.music.app.entity.Track;
import com.music.app.service.AlbumService;
import com.music.app.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;
    @Autowired
    private AlbumService albumService;

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
            @RequestParam("duration") String duration,
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
}
