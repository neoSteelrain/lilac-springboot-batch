package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface YoutubeMapper {

    int insertYoutubePlaylist(List<YoutubePlayListDTO> dto);
    int insertYoutubeVideoList(List<YoutubeVideoDTO> dto);
    int insertYoutubeChannelList(List<YoutubeChannelDTO> dto);
    int insertYoutubeChannel(YoutubeChannelDTO dto);
    int insertYoutubeCommentList(List<YoutubeCommentDTO> dto);
    List<YoutubeChannelDTO> selectChannelList(List<String> chnIds);

    @Select("SELECT id,channel_id, title, description, publish_date, view_count,subscriber_count, subscriber_count_hidden, video_count, branding_keywords, thumbnail_medium, thumbnail_high " +
            "FROM tbl_youtube_channel")
    @Results(id="findAllYoutubeChannelMap", value={
        @Result(property = "id", column = "id"),
        @Result(property = "channelId", column = "channel_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "publishDate", column = "publish_date"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "subscriberCount", column = "subscriber_count"),
        @Result(property = "subscriberCountHidden", column = "subscriber_count_hidden"),
        @Result(property = "videoCount", column = "video_count"),
        @Result(property = "brandingKeywords", column = "branding_keywords"),
        @Result(property = "thumbnailMedium", column = "thumbnail_medium"),
        @Result(property = "thumbnailHigh", column = "thumbnail_high")
    })
    List<YoutubeChannelDTO> findAllYoutubeChannels();

    @Select("SELECT count(id) FROM tbl_youtube_playlist WHERE playlist_id={#plId}")
    int checkDuplicatePl(@Param("plId")String plId);

    int checkDuplicateVideo(List<YoutubeVideoDTO> videos);
}
