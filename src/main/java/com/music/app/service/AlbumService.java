package com.music.app.service;

import com.music.app.entity.Album;
import com.music.app.repository.AlbumRepository;
import com.music.app.repository.TrackRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;

    @Autowired
    private TrackRepository trackRepository;

    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void saveAlbum(Album album) {
        albumRepository.save(album);
    }

    public String saveCoverImage(MultipartFile coverImage) throws IOException {
        String uploadDir = "src/main/resources/static/uploads/covers/";
        String fileName = UUID.randomUUID() + "-" + coverImage.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = coverImage.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/covers/" + fileName;
    }


    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }


    public Album getAlbumById(Long id) {
        return albumRepository.findById(id).orElse(null);
    }

    public List<Album> searchAlbums(String query) {
        return albumRepository.findByNameContainingIgnoreCaseOrArtistContainingIgnoreCase(query, query);
    }


    @Transactional
    public void deleteAlbum(Long albumId) {
        // Step 1: Disassociate all tracks from the album
        trackRepository.removeTracksFromAlbum(albumId);

        // Step 2: Delete the album
        albumRepository.deleteById(albumId);
    }
}
