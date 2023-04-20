package com.steelrain.lilac.batch.domain;


import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.repository.IYoutubeRepository;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import com.steelrain.lilac.batch.youtube.YoutubeDataV3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 구글 감정분석을 적용하지 않고 유튜브 데이터를 걸러내고 가져온다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeDefaultAgent implements IYoutubeAgent {

    private final IYoutubeClient m_youtubeClient;
    private final YoutubeChannelManager m_channelMgr;
    private final IYoutubeRepository m_youtubeRepository;


    @Override
    @Transactional
    public String fetchYoutubeData(String keyword, String paramToken, Integer licenseId, Integer subjectId, String[] exclusiveChannels){
        log.debug(String.format("\n==================== 키워드로 유튜브데이터 받아오기 시작, 키워드 : %s =========================", keyword));
        Map<String, Object> resultMap = m_youtubeClient.getYoutubePlayListDTO(keyword, paramToken, exclusiveChannels);
        List<YoutubePlayListDTO> playLists = (List<YoutubePlayListDTO>) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PLAY_LIST);
        String nextPageToken = (String) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PAGE_TOKEN);
        if(playLists.size() == 0){
            return paramToken;
        }
        /*
            - DB 저장순서
            1.채널정보
            2.재생목록정보
            3.영상정보
            4.코멘트정보
        */
        for(YoutubePlayListDTO pl : playLists){
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(pl.getPlayListId());
            pl.setItemCount(videos.size());
            pl.setVideos(videos); // for문을 벗어나도 videos에 접근할 수 있게 넣어준다
            if(Objects.nonNull(licenseId)){
                pl.setLicenseId(licenseId);
            }else if (Objects.nonNull(subjectId)){
                pl.setSubjectId(subjectId);
            }
        }
        m_channelMgr.initPlayList(playLists);
        m_youtubeRepository.savePlayList(playLists);
        for(YoutubePlayListDTO playlistDTO : playLists){
            for(YoutubeVideoDTO video : playlistDTO.getVideos()){
                video.setYoutubePlaylistId(playlistDTO.getId());
                video.setChannelId(playlistDTO.getChannelId());
            }
            m_youtubeRepository.saveVideoList(playlistDTO.getVideos());
            saveCommentList(playlistDTO.getVideos());
        }
        return nextPageToken;
    }
    private void saveCommentList(List<YoutubeVideoDTO> videos){
        for(YoutubeVideoDTO videoDTO : videos){
            List<YoutubeCommentDTO> comments = m_youtubeClient.getCommentList(videoDTO.getVideoId());
            for(YoutubeCommentDTO commentDTO : comments){
                commentDTO.setYoutubeId(videoDTO.getId());
            }
            log.debug("======= 댓글목록 저장전 videoDTO.getComments : {}", videoDTO.getComments());
            log.debug("======= 댓글목록 저장전 videoDTO : {}", videoDTO.toString());
            m_youtubeRepository.saveCommentList(comments);
        }
    }
}
