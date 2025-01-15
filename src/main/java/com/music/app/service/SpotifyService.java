//package com.music.app.service;
//
//import org.apache.hc.core5.http.ParseException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import se.michaelthelin.spotify.SpotifyApi;
//import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
//import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
//
//import java.io.IOException;
//import java.net.URI;
//import java.util.List;
//
//@Service
//public class SpotifyService {
//    private final SpotifyApi spotifyApi;
//
//    public SpotifyService(@Value("${spotify.client.id}") String clientId,
//                          @Value("${spotify.redirect.uri}") String redirectUri) {
//        spotifyApi = new SpotifyApi.Builder()
//                .setClientId(clientId)
//
//                .setRedirectUri(URI.create(redirectUri))
//                .build();
//    }
//
//    public List<PlaylistSimplified> getUserPlaylists(String accessToken) throws SpotifyWebApiException, IOException, IOException, ParseException, SpotifyWebApiException {
//        spotifyApi.setAccessToken(accessToken);
//        return List.of(spotifyApi.getListOfCurrentUsersPlaylists().build().execute().getItems());
//    }
//}
