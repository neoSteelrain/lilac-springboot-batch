package com.steelrain.lilac.batch.datamodel;

import com.google.api.services.youtube.model.SearchResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
public class YoutubePlayListDTO {
/*
id	bigint	NO	PRI
playlist_id	varchar(50)	NO	UNI
channel_id_fk	bigint	NO	MUL
title	varchar(100)	YES
publish_date	datetime	YES
thumbnail_medium	varchar(255)	YES
thumbnail_high	varchar(255)	YES
item_count	int	YES
reg_date	datetime	YES
channel_id	varchar(50)	YES
 */
    private Long id;
    public String playListId;
    private String title;
    private Timestamp publishDate;
    private String thumbnailMedium;
    private String thumbnailHigh;
    private Integer itemCount;

    private Long channelId; // 채널 테이블 FK
    private String channelIdOrigin; // API응답에서 반환된 채널ID 문자열

    private List<YoutubeVideoDTO> videos;

    public static YoutubePlayListDTO convertToYoutubePlayListDTO(SearchResult sr){
        YoutubePlayListDTO dto = new YoutubePlayListDTO();
        dto.setPlayListId(sr.getId().getPlaylistId());
        dto.setTitle(sr.getSnippet().getTitle());
        dto.setPublishDate(new Timestamp(sr.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnailMedium(sr.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(sr.getSnippet().getThumbnails().getHigh().getUrl());
        dto.setChannelIdOrigin(sr.getSnippet().getChannelId());
        //dto.setChannelId(sr.getSnippet().getChannelId());
        // itemCount는 재생목록의 영상들을 가져와야 알 수 있음.
        return dto;
    }
}
