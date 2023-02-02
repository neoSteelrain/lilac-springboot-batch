package com.steelrain.lilac.batch.datamodel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
public class YoutubeVideoDTO {
    private Long id;
    private Long channelId;
    private Long youtubePlaylistId; // 여기까지는 FK

    private String videoId;
    private String title;
    private String description;
    private Timestamp publishDate;
    private Timestamp regDate; // db에 insert 한 날짜, DB 자동입력된다.
    private String thumbnailDefault;
    private String thumbnailMedium;
    private String thumbnailHigh;
    private Long viewCount; // 조회수는 Video api를 따로 호출해야 가져올 수 있다
    private Integer searchCount;
    private String playlistId;
    private Long likeCount; // 좋아요는 Video api를 따로 호출해야 가져올 수 있다
    private Long favoriteCount;
    private Long commentCount;
    private String duration;
    private Float score;
    private Float magnitude;

    private List<YoutubeCommentDTO> comments;
}
