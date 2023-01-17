package com.steelrain.lilac.batch.datamodel;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Optional;

@Getter
@Setter
@ToString
public class YoutubeVideoDTO {
    /*
    id	bigint	NO	PRI
    channel_id	bigint	NO	MUL
    youtube_playlist_id	bigint	YES	MUL
    video_id	varchar(30)	NO	UNI
    title	varchar(100)	NO	MUL
    desc	varchar(5000)	YES
    publish_date	datetime	YES
    fetch_date	datetime	YES
    thumbnail_medium	varchar(255)	YES
    thumbnail_high	varchar(255)	YES
    view_count	bigint	YES
    search_count	int	YES
    playlist_id	varchar(30)	YES
    like_count	bigint	YES
    favorite_count	bigint	YES
    comment_count	bigint	YES
    duration	varchar(40)	YES
    score	double	YES
    magnitude	double	YES
     */

    private Long id;
    private Long channelId;
    private Long youtubePlaylistId; // 여기까지는 FK

    private String videoId;
    private String title;
    private String desc;
    private Timestamp publishDate;
    private Timestamp fetchDate; // db에 insert 한 날짜, DB 자동입력된다.
    private String thumbnailMedium;
    private String thumbnailHigh;
    private Long ViewCount; // 조회수는 Video api를 따로 호출해야 가져올 수 있다
    private Integer searchCount;
    private String playlistId;
    private Long likeCount; // 좋아요는 Video api를 따로 호출해야 가져올 수 있다
    private Long favoriteCount;
    private Long commentCount;
    private String duration;
    private Double score;
    private Double magnitude;

    public static Optional<YoutubeVideoDTO> convertYoutubeVideoDTO(PlaylistItem item, VideoListResponse videoInfo){
        if(videoInfo.getItems() == null || videoInfo.getItems().size() == 0)
        {
            return Optional.empty();
        }

        Video video = videoInfo.getItems().get(0);

        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setVideoId(item.getContentDetails().getVideoId());
        dto.setTitle(item.getSnippet().getTitle());
        dto.setDesc(video.getSnippet().getDescription());
        dto.setPublishDate(new Timestamp(item.getContentDetails().getVideoPublishedAt().getValue()));
        dto.setThumbnailMedium(item.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(item.getSnippet().getThumbnails().getHigh().getUrl());
        dto.setViewCount(video.getStatistics().getViewCount().longValue());
        dto.setPlaylistId(item.getSnippet().getPlaylistId());
        dto.setLikeCount(video.getStatistics().getLikeCount().longValue());
        dto.setDuration(video.getContentDetails().getDuration());

        return Optional.of(dto);
    }


}
