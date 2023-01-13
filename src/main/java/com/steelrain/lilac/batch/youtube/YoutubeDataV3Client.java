package com.steelrain.lilac.batch.youtube;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;

import java.io.IOException;

public class YoutubeDataV3Client implements IYoutubeClient{

    private final APIConfig m_apiConfig;

    public YoutubeDataV3Client(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;
    }


    @Override
    public SearchListResponse getYoutubePlayList(String keyword) {

        return searchYoutubePlayList(keyword);
    }

    private SearchListResponse searchYoutubePlayList(String keyword){
        SearchListResponse response = null;
        try{
            // 유튜브 DATA V3 API 객체 생성
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("lilac").build();

            // 키워드, 요청파라미터 설정
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(m_apiConfig.getYoutubeKey());
            search.setQ(keyword);
            search.setType("playlist");
            search.setMaxResults(50L); // API 1 call 당 최대로 가져올 수 있는 영상의 갯수는 50개
            search.setOrder("viewCount");
            search.setVideoDefinition("high");
            search.setVideoEmbeddable("any");

            // 응답필드 설정
            search.setFields("items(id/kind,id/videoId,id/playlistId,snippet/title,snippet/thumbnails/high/url,snippet/publishedAt,snippet/channelId,snippet/description,snippet/channelTitle)");
            response = search.execute(); // 유튜브 DATA V3 API 호출

        }catch(Exception e){
            throw new LilacYoutubeAPIException("유튜브 API호출 도중 예외 발생", e);
        }
        return response;
    }
}
