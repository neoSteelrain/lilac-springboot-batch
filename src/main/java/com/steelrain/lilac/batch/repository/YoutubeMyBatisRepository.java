package com.steelrain.lilac.batch.repository;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.mapper.YoutubeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Youtube API들로 데이터를 가져오고, 구글 감정분석 API를 사용해서 필터링한 데이터들을 MyBatis 를 통해 DB에 저장하는 클래스
 */
@Component
public class YoutubeMyBatisRepository implements IYoutubeRepository {

    private final YoutubeMapper m_youtubeMapper;

    public YoutubeMyBatisRepository(YoutubeMapper youtubeMapper){
        this.m_youtubeMapper = youtubeMapper;
    }

    @Override
    public int savePlayList(List<YoutubePlayListDTO> playList){
        return m_youtubeMapper.insertYoutubePlaylist(playList);
    }

    @Override
    public int saveChannelInfo(YoutubeChannelDTO channelDTO) {
        return m_youtubeMapper.insertYoutubeChannelInfo(channelDTO);
    }

    @Override
    public int saveVideoList(List<YoutubeVideoDTO> videoList) {

        return 0;
    }
}
