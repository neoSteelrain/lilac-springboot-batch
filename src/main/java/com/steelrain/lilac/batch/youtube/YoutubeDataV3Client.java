package com.steelrain.lilac.batch.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YoutubeDataV3Client implements IYoutubeClient{

    private final APIConfig m_apiConfig;

    public YoutubeDataV3Client(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;
    }



    @Override
    public List<YoutubePlayListDTO> getYoutubePlayListDTO(String keyword) {
        List<YoutubePlayListDTO> resultList = null;
        try {
            YouTube youtube = getYoutubeObject();
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setMaxResults(50L)
                    //.setOrder("date")
                    .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");

            SearchListResponse response = request.execute();
            if(response == null || (response != null && response.getItems().size() == 0)){
                return new ArrayList<>(0);
            }

            resultList = new ArrayList<>(response.getItems().size());
            for (SearchResult sr : response.getItems()){
                if(sr.isEmpty()) {
                    continue;
                }

                String playListId = sr.getId().getPlaylistId();
                if(!StringUtils.hasText(playListId)){
                    continue;
                }

                YoutubePlayListDTO dto = YoutubePlayListDTO.convertToYoutubePlayListDTO(sr);
                resultList.add(dto);
            }

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록 키워드검색 도중 예외 발생", e, keyword);
        }
        return resultList;
    }

    @Override
    public List<YoutubeVideoDTO> getVideoDTOListByPlayListId(String playListId){
        List<YoutubeVideoDTO> resultList = null;
        try {
            YouTube youtubeObj = getYoutubeObject();
            YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
            PlaylistItemListResponse response = request.setMaxResults(5L)
                    .setPlaylistId(playListId)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .execute();

            resultList = new ArrayList<>(response.getItems().size());

            List<YoutubeVideoDTO> videoDTOList = new ArrayList<>(response.getItems().size());
            for(PlaylistItem item : response.getItems()){
                Optional<YoutubeVideoDTO> dto = YoutubeVideoDTO.convertYoutubeVideoDTO(item, getVideoDetail(item.getContentDetails().getVideoId()));
                if(!dto.isPresent()){
                    continue;
                }

                videoDTOList.add(dto.get());
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
        }
        return resultList;
    }

    private YouTube getYoutubeObject() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("lilac").build();
    }

    private VideoListResponse getVideoDetail(String videoId){
        // JhKOsZuMDWs
        try {
            YouTube youTubeObj = getYoutubeObject();
            YouTube.Videos.List request = youTubeObj.videos()
                    .list("snippet,contentDetails,statistics");
            return request.setId(videoId).setKey(m_apiConfig.getYoutubeKey()).execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, videoId);
        }
    }

     /* public DateTime getDateTimeTest(){
        return DateTime.parseRfc3339(String.format("%d-01-01T00:00:00Z",LocalDateTime.now().getYear()));
    }*/

    /*getYoutubePlayListDTO 메서드로 대체하여 필요없으므로 주석처리
    @Override
    public SearchListResponse getYoutubePlayList(String keyword) {
        SearchListResponse response = null;
        try {
            YouTube youtube = getYoutubeObject();
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
            .setKey(m_apiConfig.getYoutubeKey())
            .setType("playlist")
            .setMaxResults(50L)
            //.setOrder("date")
            .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
            .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");

            response = request.execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록 키워드검색 도중 예외 발생", e, keyword);
        }
        return response;
    }*/

    /*getVideoDTOListByPlayListId 메서드로 대체 하므로 주석처리
    public PlaylistItemListResponse getVideoListByPlayListId(String playListId){
        PlaylistItemListResponse result = null;
        try {
            YouTube youtubeObj = getYoutubeObject();
            YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
            result = request.setMaxResults(5L)
                        .setPlaylistId(playListId)
                        .setKey(m_apiConfig.getYoutubeKey())
                        .execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
        }
        return result;
    }*/
}
