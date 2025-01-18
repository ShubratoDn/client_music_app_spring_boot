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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private String spotifyApiBaseUrl = "https://api.spotify.com";

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



    public List<Map<String, String>> getRecentlyPlayedTracks() {
        String accessToken = (String) session.getAttribute("spotifyAccessToken");

        if (accessToken == null) {
            return new ArrayList<>();
        }

        String url = spotifyApiBaseUrl + "/v1/me/player/recently-played";

        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the request
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Send GET request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            // Parse the response
            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
                List<Map<String, String>> tracks = new ArrayList<>();

                for (Map<String, Object> item : items) {
                    Map<String, Object> track = (Map<String, Object>) item.get("track");
                    Map<String, Object> album = (Map<String, Object>) track.get("album");
                    List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");

                    String artistNames = String.join(", ",
                            artists.stream().map(artist -> artist.get("name").toString()).toList());

                    tracks.add(Map.of(
                            "trackName", track.get("name").toString(),
                            "artistName", artistNames,
                            "albumName", album.get("name").toString(),
                            "coverImage", ((List<Map<String, Object>>) album.get("images")).get(0).get("url").toString(),
                            "previewUrl", track.get("preview_url") != null ? track.get("preview_url").toString() : "",
                            "spotifyUrl", ((Map<String, String>) track.get("external_urls")).get("spotify")
                    ));
                }

                return tracks;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }




    public List<Map<String, Object>> getUserPlaylists() {
        String accessToken = (String) session.getAttribute("spotifyAccessToken");

        if (accessToken == null) {
            return new ArrayList<>();
        }

        String url = spotifyApiBaseUrl + "/v1/me/playlists";

        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Send GET request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> playlists = (List<Map<String, Object>>) response.getBody().get("items");
                List<Map<String, Object>> playlistData = new ArrayList<>();

                for (Map<String, Object> playlist : playlists) {
                    String playlistId = (String) playlist.get("id");

                    // Fetch playlist tracks
                    List<Map<String, Object>> tracks = getPlaylistTracks(playlistId);

                    playlistData.add(Map.of(
                            "id", playlistId,
                            "name", playlist.get("name"),
                            "tracks", tracks
                    ));
                }

                return playlistData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private List<Map<String, Object>> getPlaylistTracks(String playlistId) {
        String accessToken = (String) session.getAttribute("spotifyAccessToken");

        if (accessToken == null) {
            return new ArrayList<>();
        }

        String url = spotifyApiBaseUrl + "/v1/playlists/" + playlistId + "/tracks";

        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Send GET request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
                List<Map<String, Object>> tracks = new ArrayList<>();

                for (Map<String, Object> item : items) {
                    Map<String, Object> track = (Map<String, Object>) item.get("track");
                    tracks.add(Map.of(
                            "id", track.get("id"),
                            "name", track.get("name"),
                            "duration", track.get("duration_ms"),
                            "spotifyUrl", ((Map<String, String>) track.get("external_urls")).get("spotify")
                    ));
                }

                return tracks;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }




    public List<Map<String, Object>> searchSpotify(String query, String type) {
        String accessToken = (String) session.getAttribute("spotifyAccessToken");

        if (accessToken == null) {
            return new ArrayList<>();
        }

        String url = spotifyApiBaseUrl + "/v1/search?q=" + UriUtils.encode(query, StandardCharsets.UTF_8) + "&type=" + type;

        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // Send GET request
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Result: \n\n" + response + "\n");
                Map<String, Object> searchResults = response.getBody();
                List<Map<String, Object>> results = new ArrayList<>();

                if ("track".equals(type)) {
                    List<Map<String, Object>> tracks = (List<Map<String, Object>>) ((Map<String, Object>) searchResults.get("tracks")).get("items");
                    for (Map<String, Object> track : tracks) {
                        results.add(Map.of(
                                "name", track.get("name"),
                                "artist", ((List<Map<String, Object>>) track.get("artists")).get(0).get("name"),
                                "album", ((Map<String, Object>) track.get("album")).get("name"),
                                "url", ((Map<String, Object>) track.get("external_urls")).get("spotify"),
                                "image", ((List<Map<String, Object>>) ((Map<String, Object>) track.get("album")).get("images")).get(0).get("url")
                        ));
                    }
                } else if ("artist".equals(type)) {
                    List<Map<String, Object>> artists = (List<Map<String, Object>>) ((Map<String, Object>) searchResults.get("artists")).get("items");
                    for (Map<String, Object> artist : artists) {
                        results.add(Map.of(
                                "name", artist.get("name"),
                                "url", ((Map<String, Object>) artist.get("external_urls")).get("spotify"),
                                "album","",
                                "artist", "",
                                "image", !((List<Map<String, Object>>) artist.get("images")).isEmpty()
                                        ? ((List<Map<String, Object>>) artist.get("images")).get(0).get("url")
                                        : null
                        ));
                    }
                } else if ("album".equals(type)) {
                    List<Map<String, Object>> albums = (List<Map<String, Object>>) ((Map<String, Object>) searchResults.get("albums")).get("items");
                    for (Map<String, Object> album : albums) {
                        results.add(Map.of(
                                "name", album.get("name"),
                                "album", "",
                                "artist", ((List<Map<String, Object>>) album.get("artists")).get(0).get("name"),
                                "url", ((Map<String, Object>) album.get("external_urls")).get("spotify"),
                                "image", !((List<Map<String, Object>>) album.get("images")).isEmpty()
                                        ? ((List<Map<String, Object>>) album.get("images")).get(0).get("url")
                                        : null
                        ));
                    }
                }

                return results;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }



}
