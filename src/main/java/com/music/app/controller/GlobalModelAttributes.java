package com.music.app.controller;

import com.music.app.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private SpotifyService spotifyService;

    @ModelAttribute("spotifyService")
    public SpotifyService spotifyService() {
        return spotifyService;
    }
}
