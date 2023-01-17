package com.steelrain.lilac.batch.datamodel;

import com.google.api.services.youtube.model.SearchResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class YoutubePlayListDTO {
/*
id	bigint	NO	PRI
playlist_id	varchar(50)	NO	UNI
title	varchar(100)	YES
publish_date	datetime	YES
thumbnail_medium	varchar(255)	YES
thumbnail_high	varchar(255)	YES
item_count	int	YES
 */
    private Long id;
    public String playListId;
    private String title;
    private Timestamp publishDate;
    private String thumbnail_medium;
    private String thumbnail_high;
    private Integer itemCount;

    public static YoutubePlayListDTO convertToYoutubePlayListDTO(SearchResult sr){
        YoutubePlayListDTO dto = new YoutubePlayListDTO();
        dto.setPlayListId(sr.getId().getPlaylistId());
        dto.setTitle(sr.getSnippet().getTitle());
        dto.setPublishDate(new Timestamp(sr.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnail_medium(sr.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnail_high(sr.getSnippet().getThumbnails().getHigh().getUrl());
        // itemCount는 재생목록의 영상들을 가져와야 알 수 있음.
        return dto;
    }
}
