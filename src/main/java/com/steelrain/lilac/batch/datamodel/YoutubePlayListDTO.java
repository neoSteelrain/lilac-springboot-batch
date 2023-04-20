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
channel_id	bigint	NO	MUL
title	varchar(100)	YES
publish_date	datetime	YES
thumbnail_medium	varchar(255)	YES
thumbnail_high	varchar(255)	YES
item_count	int	YES
reg_date	datetime	YES
 */
    private Long id;
    public String playListId;
    private Long channelId; // 채널 테이블 FK
    private String title;
    private Timestamp publishDate;
    private String thumbnailMedium;
    private String thumbnailHigh;
    private Integer itemCount;
    private String channelIdOrigin; // API응답에서 반환된 채널ID 문자열

    private Integer licenseId;
    private Integer subjectId;

    private List<YoutubeVideoDTO> videos;
}
