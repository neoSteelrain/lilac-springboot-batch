package com.steelrain.lilac.batch.datamodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class YoutubeCommentDTO {
    /*
    id	bigint	NO	PRI
    channel_id	bigint	NO	MUL
    youtube_id	bigint	NO	MUL

    comment_id	varchar(50)	NO	UNI
    total_reply_count	bigint	YES
    author_display_name	varchar(100)	YES
    text_original	text	YES
    text_display	text	YES
    publish_date	datetime	YES
    update_date	datetime	YES
    reply_count	int	YES
    parent_id	varchar(50)	YES
     */

    private Long id;
    private Long channelId;
    private Long youtubeId;

    private String commentId;
    private Long totalReplyCount;
    private String authorDisplayName;
    private String textOriginal;
    private String textDisplay;
    private Timestamp publishDate;
    private Timestamp updateDate;
    private Integer replyCount;
    private String parentId;
}
