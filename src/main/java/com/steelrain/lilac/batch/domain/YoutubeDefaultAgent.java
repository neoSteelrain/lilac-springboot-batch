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

import java.util.Iterator;
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
            - 재생목록의 영상이 다른 채널의 영상을 포함하는 경우가 있음
            - 다른 채널의 영상을 짜집기하여 자신의 재생목록을 만든 경우가 있음
            : 중복된 재생목록 및 영상은 저장하지 않고 저장하기 전 무조건 삭제 처리함
        */
        log.debug("중복된 재생목록을 삭제하기전 재생목록리스트 길이-{}, 재생목록리스트정보-{}",playLists.size(), playLists);
        playLists = removeDuplicatePl(playLists);
        log.debug("중복된 재생목록을 삭제후 재생목록리스트 길이-{}, 재생목록리스트정보-{}",playLists.size(), playLists);
        Iterator<YoutubePlayListDTO> iter = playLists.iterator();
        while(iter.hasNext()){
            YoutubePlayListDTO pl = iter.next();
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(pl.getPlayListId());
            if(checkDuplicateVideo(videos)){
                log.debug("중복된 영상을 포함하는 재생목록을 삭제함, 삭제된 재생목록ID-{}, 재생목록이름-{}, 재생목록정보-{}",pl.getPlayListId(), pl.getTitle(), pl);
                iter.remove();
            }else{
                pl.setVideos(videos); // for문을 벗어나도 videos에 접근할 수 있게 넣어준다
                if(Objects.nonNull(licenseId)){
                    pl.setLicenseId(licenseId);
                }else if (Objects.nonNull(subjectId)){
                    pl.setSubjectId(subjectId);
                }
            }
        }
        /*for(YoutubePlayListDTO pl : playLists){
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(pl.getPlayListId());
            pl.setItemCount(videos.size());
            pl.setVideos(videos); // for문을 벗어나도 videos에 접근할 수 있게 넣어준다
            if(Objects.nonNull(licenseId)){
                pl.setLicenseId(licenseId);
            }else if (Objects.nonNull(subjectId)){
                pl.setSubjectId(subjectId);
            }
        }*/
        m_channelMgr.initPlayList(playLists);
        m_youtubeRepository.savePlayList(playLists);
        log.debug("재생목록을 저장함, 저장된 재생목록리스트 정보-{}",playLists);
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
    private List<YoutubePlayListDTO> removeDuplicatePl(List<YoutubePlayListDTO> pl){
        Iterator<YoutubePlayListDTO> iter = pl.iterator();
        while(iter.hasNext()){
            boolean isDuplicate = m_youtubeRepository.checkDuplicatePl(iter.next().playListId);
            if(isDuplicate){
                iter.remove();
            }
        }
        return pl;
    }

    private boolean checkDuplicateVideo(List<YoutubeVideoDTO> videos){
        return m_youtubeRepository.checkDuplicateVideo(videos) > 0;
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
