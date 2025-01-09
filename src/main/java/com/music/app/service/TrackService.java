package com.music.app.service;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.music.app.entity.Album;
import com.music.app.entity.Track;
import com.music.app.repository.AlbumRepository;
import com.music.app.repository.TrackRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class TrackService {
    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;

    public TrackService(TrackRepository trackRepository, AlbumRepository albumRepository) {
        this.trackRepository = trackRepository;
        this.albumRepository = albumRepository;
    }

    public void saveTrack(String name, String duration, Long albumId, MultipartFile audioFile) throws IOException {

        String audioUrl = saveAudioFile(audioFile);

        // Extract the duration of the audio file
        duration = getAudioDuration(audioFile);


        Track track = new Track();
        track.setName(name);
        track.setDuration(duration);
        if(albumId != null){
            Album album = albumRepository.findById(albumId)
                    .orElseThrow(() ->null);
            if(album !=  null){
                track.setAlbum(album);
            }
        }
        track.setAudioUrl(audioUrl);

        trackRepository.save(track);
    }

    private String saveAudioFile(MultipartFile audioFile) throws IOException {
        // Save the file to a directory and return the URL
        String uploadDir = "src/main/resources/static/uploads/audio/";
        String fileName = UUID.randomUUID() + "-" + audioFile.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = audioFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/audio/" + fileName;
    }


    private String getAudioDuration(MultipartFile audioFile) throws IOException {
        try (InputStream inputStream = audioFile.getInputStream()) {
            // Save the file temporarily to analyze it
            File tempFile = File.createTempFile("audio", audioFile.getOriginalFilename());
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Extract audio file metadata
            AudioFile audio = AudioFileIO.read(tempFile);
            AudioHeader header = audio.getAudioHeader();
            int totalSeconds = header.getTrackLength();

            // Convert seconds into minutes and seconds (MM:SS format)
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            throw new IOException("Failed to extract audio duration", e);
        }
    }


}