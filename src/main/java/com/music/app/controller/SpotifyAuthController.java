//package com.music.app.controller;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/spotify")
//public class SpotifyAuthController {
//    @Value("${spotify.client.id}")
//    private String clientId;
//
////    @Value("${spotify.client.secret}")
//    private String clientSecret;
//
//    @Value("${spotify.redirect.uri}")
//    private String redirectUri;
//
//    @GetMapping("/login")
//    public String login() {
//        String authUrl = "https://accounts.spotify.com/authorize?" +
//                "client_id=" + clientId +
//                "&response_type=code" +
//                "&redirect_uri=" + redirectUri +
//                "&scope=user-read-private,user-read-email,playlist-modify-public,playlist-modify-private";
//        return "redirect:" + authUrl;
//    }
//
//    @GetMapping("/callback")
//    public ResponseEntity<String> callback(@RequestParam String code) {
//        // Exchange code for access token using Spotify API or WebClient
//        return ResponseEntity.ok("Spotify Authorization Successful!");
//    }
//}
