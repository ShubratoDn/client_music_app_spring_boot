package com.music.app.controller;

import com.music.app.entity.Album;
import com.music.app.service.AlbumService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/upload")
    public String showUploadAlbumForm(Model model) {
        model.addAttribute("album", new Album());
        return "upload-album";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/upload")
    public String uploadAlbum(
            @ModelAttribute Album album,
            @RequestParam("coverImage") MultipartFile coverImage,
            Model model,
            RedirectAttributes redirectAttributes) {

        StringBuilder errorMessages = new StringBuilder();

        // Validate album name
        if (album.getName() == null || album.getName().trim().isEmpty()) {
            errorMessages.append("Album name is required. ");
        }

        // Validate artist name
        if (album.getArtist() == null || album.getArtist().trim().isEmpty()) {
            errorMessages.append("Artist name is required. ");
        }

        // Validate cover image
        if (coverImage == null || coverImage.isEmpty()) {
            errorMessages.append("Cover image is required. ");
        } else if (!coverImage.getContentType().startsWith("image/")) {
            errorMessages.append("File must be an image. ");
        }

        // If there are validation errors, return them to the user
        if (errorMessages.length() > 0) {
            model.addAttribute("error", errorMessages.toString());
            model.addAttribute("album", album); // Pass the album back to the model
            return "upload-album";
        }

        // If validation passes, proceed with saving the album
        try {
            String coverImageUrl = albumService.saveCoverImage(coverImage);
            album.setCoverImageUrl(coverImageUrl);
            albumService.saveAlbum(album);
            redirectAttributes.addFlashAttribute("success", "Album uploaded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload album: " + e.getMessage());
        }

        return "redirect:/albums/upload";
    }


    @GetMapping
    public String showAlbumList(Model model) {
        List<Album> albums = albumService.getAllAlbums(); // Fetch all albums from the service
        model.addAttribute("albums", albums);
        return "album-list"; // View name
    }

    @GetMapping("/view/{id}")
    public String viewAlbum(@PathVariable Long id, Model model) {
        Album album = albumService.getAlbumById(id); // Fetch the album using the ID
        if (album == null) {
            model.addAttribute("error", "Album not found!");
            return "redirect:/albums"; // Redirect to album list if not found
        }
        model.addAttribute("album", album);
        return "album-view"; // The Thymeleaf template for viewing album details
    }



}
