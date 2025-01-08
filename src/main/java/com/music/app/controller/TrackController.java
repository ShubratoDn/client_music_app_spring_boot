package com.music.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrackController {

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/add-track")
    public String showAddTrackForm() {
        return "add-track";
    }
}
