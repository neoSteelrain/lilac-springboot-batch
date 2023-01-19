package com.steelrain.lilac.batch.repository;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;

import java.util.List;

public interface IYoutubeRepository {
    int savePlayList(List<YoutubePlayListDTO> playList);
    int saveChannelInfo(YoutubeChannelDTO channelDTO);
    int saveVideoList(List<YoutubeVideoDTO> videoList);
    int saveCommentList(List<YoutubeCommentDTO> commentList);
}
