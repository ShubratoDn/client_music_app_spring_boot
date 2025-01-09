package com.music.app.controller;

import com.music.app.entity.Album;
import com.music.app.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AlbumService albumService;

    @GetMapping("/")
    public String index(Model model) {
        List<Album> albums = albumService.getAllAlbums(); // Fetch all albums from the service
        model.addAttribute("albums", albums);
        return "index";
    }

    @GetMapping("/user/dashboard")
    public String showUserDashboard(Model model) {
        List<Album> albums = albumService.getAllAlbums(); // Fetch all albums from the service
        model.addAttribute("albums", albums);
        return "index";
    }
}
