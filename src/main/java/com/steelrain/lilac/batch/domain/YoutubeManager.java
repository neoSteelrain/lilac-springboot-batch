package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.as.ISentimentClient;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.*;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import com.steelrain.lilac.batch.repository.IYoutubeRepository;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 유튜브 배치작업의 퍼사드
 * - 작업내용 및 순서
 *  1. 미리등록된 키워드 (subject, license)로 Search.list 메서드를 호출한다
 *  2. 결과중에서 playlistId 가 있는 영상만 추출한다
 *  3. 추출된 영상의 playlistId 를 가지고 PlaylistItem 메서드로 해당 재생목록의 모든 영상을 가져온다
 *  4. 재생목록에 속한 영상들의 코멘트들을 전부 가져온다
 *  5. 코멘트들에 감정분석을 적용해서 긍정수치가 70% 이상(또는 부정보다는 긍정이 높은) 이라면 영상정보, 재생목록정보, 채널정보를 DB에 저장한다
 * - 사용자에게 제공하는 검색서비스는 DB에 저장된 재생목록의 제목을 대상으로 하거나, 개별적인 영상들의 제목을 검색하게 된다.
 *
 * - DB 저장순서
 * 1. 재생목록정보
 * 2. 채널정보
 * 3. 영상정보
 * 4. 코멘트정보
 */
@Slf4j
@Component
public class YoutubeManager {

    private final KeywordMapper m_subjectMapper;
    private final IYoutubeClient m_youtubeClient;
    private final ISentimentClient m_sentimentClient;
    private final APIConfig m_apiConfig;
    private final CommentByteCounter m_commentByteCounter;
    private final IYoutubeRepository m_youtubeRepository;

    public YoutubeManager(KeywordMapper subjectMapper,
                          IYoutubeClient youtubeClient,
                          ISentimentClient sentimentClient,
                          APIConfig apiConfig,
                          CommentByteCounter commentByteCounter,
                          IYoutubeRepository repository){
        this.m_subjectMapper = subjectMapper;
        this.m_youtubeClient = youtubeClient;
        this.m_sentimentClient = sentimentClient;
        this.m_apiConfig = apiConfig;
        this.m_commentByteCounter = commentByteCounter;
        this.m_youtubeRepository = repository;
    }


    /*public void dispatchYoutube(){
        List<KeywordSubjectDTO> subjectList = getSubjectList();
        List<KeywordLicenseDTO> licenseList = getLicenseList();

        // 정보처리기사만 가지고 작업
        String keyword = "정보처리기사";
        IYoutubeClient youtubeClient = new YoutubeDataV3Client(m_apiConfig);
        SearchListResponse youtubeResponse = youtubeClient.getYoutubePlayList(keyword);
        //youtubeResponse.getItems().get(1).getId().getPlaylistId()
        List<SearchResult> items = youtubeResponse.getItems();
        for(SearchResult item : items){
            if(item.isEmpty()){
                continue;
            }

            YoutubePlayListDTO playListDTO = YoutubePlayListDTO.convertToYoutubePlayListDTO(item);

            String playListId = item.getId().getPlaylistId();
            if(!StringUtils.hasText(playListId)){
                continue;
            }

            // 재생목록에 있는 모든 영상을 가져온다.

        }
    }*/

    @Transactional
    public void dispatchYoutube(){
        List<KeywordSubjectDTO> subjectList = getSubjectList();
        List<KeywordLicenseDTO> licenseList = getLicenseList();

        // "정보처리기사" 키워드로 테스트
        String keyword = "정보처리기사";
        List<YoutubePlayListDTO> playLists = m_youtubeClient.getYoutubePlayListDTO(keyword);

        YoutubeChannelDTO channelDTO = null;
        if(playLists != null && playLists.size() >= 1){
            channelDTO = m_youtubeClient.getChannelInfo(playLists.get(0).getChannelId());
        }

        Iterator<YoutubePlayListDTO> iter = playLists.iterator();
        while(iter.hasNext()){
           // - 재생목록에 있는 모든 영상을 가져오고 감정분석으로 걸러낸다.
           // - 부정적인 댓글을 가진 영상이 있는 재생목록은 삭제한다.
            YoutubePlayListDTO playlist = iter.next();
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(playlist.playListId);
            if(!hasPositiveCommentAndLinkedComments(videos)){
                iter.remove();
                continue;
            }
            playlist.setItemCount(videos.size());
            playlist.setVideos(videos);
        }

        // insert 순서 : 재생목록, 채널정보, 유튜브영상, 댓글
        m_youtubeRepository.savePlayList(playLists);
        m_youtubeRepository.saveChannelInfo(channelDTO);
        for(YoutubePlayListDTO playlist : playLists){
            for(YoutubeVideoDTO video : playlist.getVideos()){
                video.setYoutubePlaylistId(playlist.getId());
                video.setChannelId(channelDTO.getId());
            }
            log.debug( String.format("============== playlist id :  %s  ================= video list insert 시작 ==========================================", playlist.playListId));
            m_youtubeRepository.saveVideoList(playlist.getVideos());
            log.debug( String.format("============== playlist id :  %s  ================= video list insert 끝 ==========================================", playlist.playListId));
//            for(YoutubeVideoDTO video : playlist.getVideos()){
//
//                for(YoutubeCommentDTO dto : video.getComments()) { //  TODO : NPE
//                    dto.setYoutubeId(video.getId());
//                    dto.setChannelId(channelDTO.getId());
//                }
//                m_youtubeRepository.saveCommentList(video.getComments());
//            }
        }
    }

    // 영상들의 댓글리스트를 가져오고 감정분석을 하여 긍정수치가 부정적인 댓글을 가진 유튜브영상들의 리스트를 새로만들어서 반환
    private List<YoutubeVideoDTO> filterVideoListAndNewList(List<YoutubeVideoDTO> videos){
        List<YoutubeVideoDTO> resultList = new ArrayList<>(videos.size());
        for(YoutubeVideoDTO video : videos){
            List<YoutubeCommentDTO> comments = m_youtubeClient.getCommentList(video.getVideoId());
            if(analyzeComment(comments, video)){
                resultList.add(video);
            }
        }
        return resultList;
    }

    private List<YoutubeVideoDTO> filterVideoList(List<YoutubeVideoDTO> videos){
        Iterator<YoutubeVideoDTO> iter = videos.iterator();
        while(iter.hasNext()){
            YoutubeVideoDTO video = iter.next();
            List<YoutubeCommentDTO> comments = m_youtubeClient.getCommentList(video.getVideoId());
            if(!analyzeComment(comments, video)){
                iter.remove();
            }
        }
        return videos;
    }

    /*
        - 각 영상의 댓글리스트을 가져온다
        - 부정적인 댓글이 있으면 바로 false리턴
        - 부정적이지 않은 댓글이 있는 영상에는 가져온 댓글리스트를 설정한다.
     */
    private boolean hasPositiveCommentAndLinkedComments(List<YoutubeVideoDTO> videos){
        for(YoutubeVideoDTO video : videos){
            if(video.getCommentCount() <= 0){
                continue;
            }
            List<YoutubeCommentDTO> comments = null;
            try{
                comments = m_youtubeClient.getCommentList(video.getVideoId());
                if(!analyzeComment(comments, video)){ // 부정적인 댓글이 있다면 즉시 false 리턴
                    return false;
                }
            }catch(LilacYoutubeAPIException le){
                log.error("댓글 감정분석중 예외 발생 : 예외가 발생한 video id = " + video.getVideoId(), le);
                continue;
            }
            video.setComments(comments);
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

        SentimentDTO sentimentDTO = m_sentimentClient.analyizeComment(assembledComment);

        log.debug("감정분석 결과 : " + sentimentDTO.toString());

        if(sentimentDTO.getScore() >= m_apiConfig.getThreshold()){ // 긍정수치가 임계치보다 높거나 같다면 통과시키고, 유튜브영상에 감정수치를 입력한다.
            video.setScore(sentimentDTO.getScore());
            video.setMagnitude(sentimentDTO.getMagnitude());
            return true;
        }
        return false;
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




    // 미리등록된 검색키워드를 목록으로 가져온다.
    private List<KeywordSubjectDTO> getSubjectList(){
        return m_subjectMapper.getSubjectList();
    }

    // 미리등록된 자격증정보를 목록으로 가져온다.
    private List<KeywordLicenseDTO> getLicenseList(){
        return m_subjectMapper.getLicenseList();
    }
}
