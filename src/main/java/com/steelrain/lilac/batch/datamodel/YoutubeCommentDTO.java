package com.steelrain.lilac.batch.datamodel;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class YoutubeCommentDTO {
    private Long id;
    private Long youtubeId;
    private String commentId;
    private Long totalReplyCount;
    private String authorDisplayName;
    private String textOriginal;
    private String textDisplay;
    private Timestamp publishDate;
    private Timestamp updateDate;
    private String parentId;
    private String channelId;
}
