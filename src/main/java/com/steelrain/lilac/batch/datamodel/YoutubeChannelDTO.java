package com.steelrain.lilac.batch.datamodel;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class YoutubeChannelDTO {
    /*
    id	bigint	NO	PRI
    channel_id	varchar(50)	NO	UNI
    title	varchar(100)	YES
    desc	varchar(1000)	YES
    publish_date	datetime	YES
    view_count	bigint unsigned	YES
    subscriber_count	bigint unsigned	YES
    subscriber_count_hidden	tinyint(1)	YES
    video_count	bigint unsigned	YES
    branding_keywords	varchar(255)	YES
    thumbnail_medium	varchar(255)	YES
    thumbnail_high	varchar(255)	YES
     */

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
}
