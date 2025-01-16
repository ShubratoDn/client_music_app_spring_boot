package com.music.app.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SpotifyService {

    @Autowired
    private RestTemplate restTemplate;


    private final HttpSession session;

    public SpotifyService(HttpSession session) {
        this.session = session;
    }

    private String accessToken;
    private Map<String, String> spotifyUserInfo = new HashMap<>();

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public boolean isConnected() {
        return session.getAttribute("spotifyAccessToken") != null;
    }

    public Map<String, String> getUserInfo() {
        Object userInfo = session.getAttribute("spotifyUserInfo");
        if (userInfo instanceof Map) {
            return (Map<String, String>) userInfo;
        }
        return new HashMap<>();
    }

    public void fetchAndStoreUserInfo() {
        if (accessToken != null) {
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "https://api.spotify.com/v1/me";
//
//            Map<String, String> headers = new HashMap<>();
//            headers.put("Authorization", "Bearer " + accessToken);
//
//            Map response = restTemplate.getForObject(url, Map.class, headers);
//
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>("paramters", headers);

            ResponseEntity<Object> response = restTemplate.exchange("https://api.spotify.com/v1/me", HttpMethod.GET, entity, Object.class);
            LinkedHashMap result = (LinkedHashMap) response.getBody();
            spotifyUserInfo.put("displayName", (String) result.get("display_name"));
            spotifyUserInfo.put("email", (String) result.get("email"));
            // Store user info in session
            session.setAttribute("spotifyUserInfo", spotifyUserInfo);
        }
    }

    public void clearSpotifySession() {
        session.removeAttribute("spotifyAccessToken");
        session.removeAttribute("spotifyUserInfo");
    }


    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;


    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final String spotifyAuthUrl = "https://accounts.spotify.com/authorize";
    private final String spotifyTokenUrl = "https://accounts.spotify.com/api/token";

    public String getAuthorizationUrl() {
        return "https://accounts.spotify.com/en/authorize?client_id=" + clientId
                + "&response_type=code&redirect_uri=" + redirectUri
                + "&scope=ugc-image-upload,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,streaming,app-remote-control,user-read-email,user-read-private"
                + ",playlist-read-collaborative,playlist-modify-public,playlist-read-private,playlist-modify-private,user-library-modify,user-library-read,user-top-read,user-read-playback-position,user-read-recently-played,user-follow-read,user-follow-modify";
    }

    public String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String credentials = clientId + ":" + clientSecret;
        headers.setBasicAuth(Base64.getEncoder().encodeToString(credentials.getBytes()));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(spotifyTokenUrl, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            session.setAttribute("spotifyAccessToken", response.getBody().get("access_token").toString());
            return response.getBody().get("access_token").toString();
        }
        return null;
    }
}
