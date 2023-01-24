package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface YoutubeMapper {

    int insertYoutubePlaylist(List<YoutubePlayListDTO> dto);
    int insertYoutubeVideoList(List<YoutubeVideoDTO> dto);
    int insertYoutubeChannelList(List<YoutubeChannelDTO> dto);
    int insertYoutubeCommentList(List<YoutubeCommentDTO> dto);
    List<YoutubeChannelDTO> selectChannelList(List<String> chnIds);

}
