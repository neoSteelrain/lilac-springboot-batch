package com.steelrain.lilac.batch.repository;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;

import java.util.List;

public interface IYoutubeRepository {
    int savePlayList(List<YoutubePlayListDTO> playList);
    int saveChannelList(List<YoutubeChannelDTO> chnList);
    int saveChannel(YoutubeChannelDTO chnDTO);
    int saveVideoList(List<YoutubeVideoDTO> videoList);
    int saveCommentList(List<YoutubeCommentDTO> commentList);
    List<YoutubeChannelDTO> getChannelList(List<String> chnIdList);
    List<YoutubeChannelDTO> findAllYoutubeChannels();
}
