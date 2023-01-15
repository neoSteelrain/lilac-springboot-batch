package com.steelrain.lilac.batch.youtube;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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
        /*
        - 검색 작업순서
        1. 미리등록된 키워드 (subject, license)로 Search.list 메서드를 호출한다
        2. 결과중에서 playlistId 가 있는 영상만 추출한다
        3. 추출된 영상의 playlistId 를 가지고 PlaylistItem 메서드로 해당 재생목록의 모든 영상을 가져온다
        4. 재생목록에 속한 영상들의 코멘트들을 전부 가져온다
        5. 코멘트들에 감정분석을 적용해서 긍정수치가 70% 이상(또는 부정보다는 긍정이 높은) 이라면 영상정보, 재생목록정보, 채널정보를 DB에 저장한다

        # 사용자에게 제공하는 검색서비스는 DB에 저장된 재생목록의 제목을 대상으로 하거나, 개별적인 영상들의 제목을 검색하게 된다.
        
        - DB 저장순서
        1. 재생목록정보
        2. 채널정보
        3. 영상정보
        4. 코멘트정보
         */
        return searchYoutubePlayList(keyword);
    }

    private SearchListResponse searchYoutubePlayList(String keyword){
        SearchListResponse response = null;
        try{
            // 유튜브 DATA V3 API 객체 생성
            // YouTube youtube = new YouTube.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), new HttpRequestInitializer() {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("lilac").build();

            // 키워드, 요청파라미터 설정
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(m_apiConfig.getYoutubeKey());
            search.setQ(keyword);
            search.setType("video");
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
