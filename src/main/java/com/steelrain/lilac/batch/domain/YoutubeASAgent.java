package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.as.ISentimentClient;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.*;
import com.steelrain.lilac.batch.repository.IYoutubeRepository;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import com.steelrain.lilac.batch.youtube.YoutubeDataV3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 구글 감정분석을 사용하여 유튜브 데이터를 걸러내서 가져온다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeASAgent implements IYoutubeAgent{
    private final IYoutubeClient m_youtubeClient;
    private final ISentimentClient m_sentimentClient;
    private final APIConfig m_apiConfig;
    private final CommentByteCounter m_commentByteCounter;
    private final IYoutubeRepository m_youtubeRepository;
    private final YoutubeChannelManager m_channelMgr;


    @Override
    @Transactional
    public String fetchYoutubeData(String keyword, String paramToken, Integer licenseId, Integer subjectId, String[] exclusiveChannels) {
        log.debug(String.format("\n==================== 키워드로 유튜브데이터 받아오기 시작, 키워드 : %s =========================", keyword));
        // insert 순서 : 채널정보, 재생목록, 유튜브영상목록, 댓글목록
        Map<String, Object> resultMap = m_youtubeClient.getYoutubePlayListDTO(keyword, paramToken, exclusiveChannels);
        List<YoutubePlayListDTO> playLists = (List<YoutubePlayListDTO>) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PLAY_LIST);
        if(playLists.size() == 0){
            return paramToken;
        }
        String nextPageToken = (String) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PAGE_TOKEN);

        Iterator<YoutubePlayListDTO> iter = playLists.iterator();
        boolean isPositive = false;
        while(iter.hasNext()){
            // - 재생목록에 있는 모든 영상을 가져오고 감정분석으로 걸러낸다.
            // - 부정적인 댓글을 가진 영상이 있는 재생목록은 삭제한다.
            YoutubePlayListDTO playlist = iter.next();
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(playlist.playListId);
            log.debug("유튜브 API로 가져온 영상목록정보 : 리스트길이 - {}, 리스트정보 - {}", videos.size(), videos.toString());
            isPositive = hasPositiveCommentAndLinkComments(videos);
            if(!isPositive) {
                iter.remove();
                continue;
            }
            log.debug("감정분석단계를 지난 영상목록정보 : 리스트길이 - {}, 리스트정보 - {}", videos.size(), videos.toString());
            playlist.setItemCount(videos.size());
            playlist.setVideos(videos);
            if(Objects.nonNull(licenseId)){
                playlist.setLicenseId(licenseId);
            }else if (Objects.nonNull(subjectId)){
                playlist.setSubjectId(subjectId);
            }
        }
        if(!isPositive){
            return nextPageToken;
        }
        // 아래의 코드를 while문 안으로 넣으면 벌크 insert 할 수 없으므로 while문 밖에서 처리한다
        m_channelMgr.initPlayList(playLists); // 재생목록에 있는 채널들의 정보를 초기화하고 재생목록의 DB 채널 id를 업데이트 해준다
        m_youtubeRepository.savePlayList(playLists);
        for(YoutubePlayListDTO playlistDTO : playLists){
            for(YoutubeVideoDTO video : playlistDTO.getVideos()){
                video.setYoutubePlaylistId(playlistDTO.getId());
                video.setChannelId(playlistDTO.getChannelId());
            }
            log.debug("DB에 저장하기 직전 재생목록에 있는 영상목록정보 : 리스트길이 - {}. 리스트정보 - {}",playlistDTO.getVideos().size(), playlistDTO.getVideos().toString());
            m_youtubeRepository.saveVideoList(playlistDTO.getVideos());
            saveCommentList(playlistDTO.getVideos());
        }
        return nextPageToken;
    }

    private void saveCommentList(List<YoutubeVideoDTO> videos){
        for(YoutubeVideoDTO videoDTO : videos){
            log.debug("======= 댓글목록 저장전 videoDTO.getComments : {}", videoDTO.getComments());
            log.debug("======= 댓글목록 저장전 videoDTO : {}", videoDTO.toString());
            for(YoutubeCommentDTO commentDTO : videoDTO.getComments()){
                commentDTO.setYoutubeId(videoDTO.getId());
            }
            m_youtubeRepository.saveCommentList(videoDTO.getComments());
        }
    }


    /*
        - 각 영상의 댓글리스트을 유튜브API로 가져온다
        - 부정적인 댓글이 있으면 바로 false리턴
        - 부정적이지 않은 댓글이 있는 영상에는 가져온 댓글리스트를 설정하고 true 리턴
    */
    private boolean hasPositiveCommentAndLinkComments(List<YoutubeVideoDTO> videos){
        boolean isSentimentActive = m_apiConfig.getSentimentActive();
        for(YoutubeVideoDTO video : videos){
            try{
                List<YoutubeCommentDTO> comments = m_youtubeClient.getCommentList(video.getVideoId());
                if(isSentimentActive){
                    if(!analyzeComment(comments, video)){ // 부정적인 댓글이 있다면 즉시 false 리턴 . 감정분석 API 할당량 때문에 테스트할때는 주석처리한다
                        return false;
                    }
                }else{
                    video.setComments(comments);
                }
            }catch(Exception ex){
                log.error("댓글 감정분석중 예외 발생 : 예외가 발생한 video id = {}, 예외정보 : {}", video.getVideoId(), ex);
                video.setComments(new ArrayList<>(0));
                return false;
            }
        }
        return true;
    }

    /*
         - 댓글리스트의 댓글들을 1000바이트까지만 모아서 감정분석을 하고
           긍정적인 댓글을 가진 영상이 있으면 true, 부정적인이면 false 리턴
         - 영상의 score, magnitude 속성을 감정분석 결과값으로 설정한다.
    */
    private boolean analyzeComment(List<YoutubeCommentDTO> comments, YoutubeVideoDTO video){
        String assembledComment = assembleComment(comments);

        log.debug("video.getTitle() : " + video.getTitle());
        log.debug("videoId : " + video.getVideoId());
        log.debug("comments.size() : " + comments.size());
        log.debug("assembledComment : " + assembledComment);

        try{
            SentimentDTO sentimentDTO = m_sentimentClient.analyzeComment(assembledComment);
            log.debug("감정분석 결과 : " + sentimentDTO.toString());

            if(sentimentDTO.getScore() >= m_apiConfig.getThreshold()){ // 긍정수치가 임계치보다 높거나 같다면 통과시키고, 유튜브영상에 감정수치를 입력한다.
                video.setScore(sentimentDTO.getScore());
                video.setMagnitude(sentimentDTO.getMagnitude());
                return true;
            }
            return false;
        }catch (Exception ex){
            log.error("댓글 감정분석중 예외 발생 : 예외가 발생한 video id = {}, 예외정보 : {}", video.getVideoId(), ex);
            throw ex;
        }
    }

    // 댓글목록에서 댓글들을 더하여 1000바이트 댓글문자열을 만들어서 반환한다
    private String assembleComment(List<YoutubeCommentDTO> comments){
        for(YoutubeCommentDTO dto : comments){
            if(m_commentByteCounter.isAddable(dto.getTextOriginal())){
                m_commentByteCounter.addComment(dto.getTextOriginal());
            }else{
                break;
            }
        }
        return m_commentByteCounter.getTotalComment();
    }
}
