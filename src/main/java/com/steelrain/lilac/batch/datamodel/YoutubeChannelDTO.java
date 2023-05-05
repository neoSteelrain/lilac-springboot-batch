package com.steelrain.lilac.batch.datamodel;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class YoutubeChannelDTO {
    private Long id;
    private String channelId;
    private String title;
    private String description;
    private Timestamp publishDate;
    private Long viewCount;
    private Long subscriberCount;
    private Boolean subscriberCountHidden;
    private Long videoCount;
    private String brandingKeywords;
    private String thumbnailMedium;
    private String thumbnailHigh;
    private String customUrl;
}
