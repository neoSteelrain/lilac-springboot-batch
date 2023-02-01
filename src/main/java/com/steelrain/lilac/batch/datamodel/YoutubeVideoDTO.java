package com.steelrain.lilac.batch.datamodel;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.Video;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
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

    // 영상정보의 값이 일정하게 넘어오지 않는다. 때때로 속성값이 있거나 없거나(null) 일정하게 넘어오지 않는다.
    public static YoutubeVideoDTO convertYoutubeVideoDTO(PlaylistItem item, Video videoInfo){
        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setVideoId(item.getContentDetails().getVideoId());
        dto.setTitle(item.getSnippet().getTitle());
        dto.setPlaylistId(item.getSnippet().getPlaylistId());
        dto.setPublishDate(new Timestamp(item.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnailMedium(item.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(item.getSnippet().getThumbnails().getHigh().getUrl());

        dto.setViewCount(videoInfo.getStatistics().getViewCount() == null ? 0 : videoInfo.getStatistics().getViewCount().longValue());
        dto.setDescription(videoInfo.getSnippet().getDescription());
        dto.setLikeCount(videoInfo.getStatistics().getLikeCount() == null ? 0 : videoInfo.getStatistics().getLikeCount().longValue());
        dto.setCommentCount( videoInfo.getStatistics().getCommentCount() == null && videoInfo.getStatistics().getCommentCount().longValue() == 0 ? 0 : videoInfo.getStatistics().getCommentCount().longValue());
        dto.setDuration(videoInfo.getContentDetails().getDuration());

        return dto;
    }

    public static YoutubeVideoDTO convertYoutubeVideoDTOnoDetail(PlaylistItem item){
        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setVideoId(item.getContentDetails().getVideoId());
        dto.setTitle(item.getSnippet().getTitle());
        dto.setPlaylistId(item.getSnippet().getPlaylistId());
        dto.setPublishDate(new Timestamp(item.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnailDefault(item.getSnippet().getThumbnails().getDefault().getUrl());
        dto.setThumbnailMedium(item.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(item.getSnippet().getThumbnails().getHigh().getUrl());

        dto.setViewCount(0L);
        dto.setDescription(item.getSnippet().getDescription());
        dto.setLikeCount(0L);
        dto.setCommentCount(0L);
        dto.setDuration(null);

        return dto;
    }
    /*public static Optional<YoutubeVideoDTO> convertYoutubeVideoDTO(PlaylistItem item, VideoListResponse videoInfo){
        if(item.getSnippet() == null || item.getContentDetails() == null){
           return Optional.empty();
        }

        if(videoInfo.getItems() == null || videoInfo.getItems().size() == 0) {
            return Optional.empty();
        }
        Video video = videoInfo.getItems().get(0);
        if(video.getStatistics() == null || video.getSnippet() == null || video.getContentDetails() == null){
            return Optional.empty();
        }

        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setVideoId(item.getContentDetails().getVideoId());
        dto.setTitle(item.getSnippet().getTitle());
        dto.setPlaylistId(item.getSnippet().getPlaylistId());
        dto.setPublishDate(new Timestamp(item.getContentDetails().getVideoPublishedAt().getValue()));
        dto.setThumbnailMedium(item.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(item.getSnippet().getThumbnails().getHigh().getUrl());

        dto.setViewCount(video.getStatistics().getViewCount() == null ? 0 : video.getStatistics().getViewCount().longValue());
        dto.setDesc(video.getSnippet().getDescription());
        dto.setLikeCount(video.getStatistics().getLikeCount() == null ? 0 : video.getStatistics().getLikeCount().longValue());
        dto.setDuration(video.getContentDetails().getDuration());

        return Optional.of(dto);
    }*/


}
