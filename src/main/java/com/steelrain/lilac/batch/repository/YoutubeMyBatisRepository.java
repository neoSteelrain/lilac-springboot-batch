package com.steelrain.lilac.batch.repository;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.mapper.YoutubeMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Youtube API로 데이터를 가져오고, 구글 감정분석 API를 사용해서 필터링한 데이터들을 MyBatis 를 통해 DB에 저장하는 클래스
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
    public int saveChannelList(List<YoutubeChannelDTO> chnList) {
        return m_youtubeMapper.insertYoutubeChannelList(chnList);
    }

    @Override
    public int saveChannel(YoutubeChannelDTO chnDTO) {
        return m_youtubeMapper.insertYoutubeChannel(chnDTO);
    }

    @Override
    public int saveVideoList(List<YoutubeVideoDTO> videoList) {
        return m_youtubeMapper.insertYoutubeVideoList(videoList);
    }

    @Override
    public int saveCommentList(List<YoutubeCommentDTO> commentList) {
        if(commentList.size() == 0){
            return 0;
        }
        return m_youtubeMapper.insertYoutubeCommentList(commentList);
    }

    @Override
    public List<YoutubeChannelDTO> getChannelList(List<String> chnIdList) {
        return m_youtubeMapper.selectChannelList(chnIdList);
    }

    @Override
    public List<YoutubeChannelDTO> findAllYoutubeChannels() {
        return m_youtubeMapper.findAllYoutubeChannels();
    }

    @Override
    public boolean checkDuplicatePl(String plId) {
        return m_youtubeMapper.checkDuplicatePl(plId) > 0;
    }

    @Override
    public int checkDuplicateVideo(List<YoutubeVideoDTO> videos) {
        return m_youtubeMapper.checkDuplicateVideo(videos);
    }
}
