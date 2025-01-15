package com.music.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity

public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String duration;
    private String audioUrl;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(mappedBy = "tracks")
    private List<Playlist> playlists = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Album getAlbum() {
        if(album != null) album.setTracks(null);
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @JsonIgnore
    public List<Playlist> getPlaylists() {
        return playlists;
    }

    @JsonProperty
    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", duration='" + duration + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                ", album=" + album +
                ", playlists=" + playlists +
                '}';
    }
}
