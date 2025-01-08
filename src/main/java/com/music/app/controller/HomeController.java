package com.music.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/user/dashboard")
    public String showUserDashboard() {
        return "index";
    }

    @GetMapping("/user/dashboard/test")
    public String showUserDashboardX() {
        return "index";
    }
}
