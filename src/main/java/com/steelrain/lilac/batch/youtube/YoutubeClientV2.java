package com.steelrain.lilac.batch.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class YoutubeClientV2 implements IYoutubeClient{

    private final APIConfig m_apiConfig;

    public YoutubeClientV2(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;
    }

   /* public DateTime getDateTimeTest(){
        return DateTime.parseRfc3339(String.format("%d-01-01T00:00:00Z",LocalDateTime.now().getYear()));
    }*/

    @Override
    public SearchListResponse getYoutubePlayList(String keyword) {
        SearchListResponse response = null;
        try {
            YouTube youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("lilac").build();

            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
                    .setType("playlist")
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setMaxResults(50L)
            // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setPublishedAfter(DateTime.parseRfc3339(String.format("%d-01-01T00:00:00Z",LocalDateTime.now().getYear())));
//            request.setOrder("viewCount");
//            request.setVideoDefinition("high");
//            request.setVideoEmbeddable("any");

            response = request.execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 API호출 도중 예외 발생", e);
        }
        return response;
    }
}
