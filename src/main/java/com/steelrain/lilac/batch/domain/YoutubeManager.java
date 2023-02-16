package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.as.ISentimentClient;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.*;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;
import com.steelrain.lilac.batch.repository.IYoutubeRepository;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import com.steelrain.lilac.batch.youtube.YoutubeDataV3Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 유튜브 배치작업의 퍼사드
 * - 작업내용 및 순서
 *  1. 미리등록된 키워드 (subject, license)로 재생목록을 검색한다
 *  2. 결과중에서 playlistId 가 있는 영상만 추출한다
 *  3. 추출된 영상의 playlistId 를 가지고 PlaylistItem API로 해당 재생목록의 모든 영상을 가져온다
 *  4. 재생목록에 속한 영상들의 코멘트들을 설정만큼 가져온다
 *  5. 코멘트들에 감정분석을 적용해서 긍정수치가 설정파일에 설정된 수치와 같거나 크다면 영상정보, 재생목록정보, 채널정보를 DB에 저장한다
 * - 사용자에게 제공하는 검색서비스는 DB에 저장된 재생목록의 제목을 대상으로 하거나, 개별적인 영상들의 제목을 검색하게 된다.
 *
 * - DB 저장순서
 * 1.채널정보
 * 2.재생목록정보
 * 3.영상정보
 * 4.코멘트정보
 */
@Slf4j
@Component
public class YoutubeManager {
    private final IYoutubeClient m_youtubeClient;
    private final ISentimentClient m_sentimentClient;
    private final APIConfig m_apiConfig;
    private final CommentByteCounter m_commentByteCounter;
    private final IYoutubeRepository m_youtubeRepository;
    private final KeywordManager m_keywordManager;
    private final YoutubeChannelManager m_channelManager;


    public YoutubeManager(IYoutubeClient youtubeClient,
                          ISentimentClient sentimentClient,
                          APIConfig apiConfig,
                          CommentByteCounter commentByteCounter,
                          IYoutubeRepository repository,
                          KeywordManager keywordManager,
                          YoutubeChannelManager channelManager){
        this.m_youtubeClient = youtubeClient;
        this.m_sentimentClient = sentimentClient;
        this.m_apiConfig = apiConfig;
        this.m_commentByteCounter = commentByteCounter;
        this.m_youtubeRepository = repository;
        this.m_keywordManager = keywordManager;
        this.m_channelManager = channelManager;
    }

    @Transactional
    public void doYoutubeBatch(){
        doSubjectBatch();
        doLicenseBatch();
    }

    private void doLicenseBatch() {
        List<KeywordLicenseDTO> licenseList = m_keywordManager.getLicenseList();
        for (KeywordLicenseDTO licenseDTO : licenseList) {
            String nextPageToken = fetchYoutubeData(licenseDTO.getKeyWord(), licenseDTO.getPageToken(), m_apiConfig.getExclusiveChannels());
            LicenseBatchResultDTO batchResultDTO = LicenseBatchResultDTO.builder()
                    .id(licenseDTO.getId())
                    .pageToken(nextPageToken)
                    .build();
            m_keywordManager.updateLicensePageToken(batchResultDTO);
        }
    }

    private void doSubjectBatch(){
        List<KeywordSubjectDTO> subjectList = m_keywordManager.getSubjectList();
        for (KeywordSubjectDTO subjectDTO : subjectList){
            String pageToken = fetchYoutubeData(subjectDTO.getKeyWord(), subjectDTO.getPageToken(), null);
            SubjectBatchResultDTO batchResultDTO = SubjectBatchResultDTO.builder()
                    .id(subjectDTO.getId())
                    .pageToken(pageToken)
                    .build();
            m_keywordManager.updateSubjectPageToken(batchResultDTO);
        }
    }



    /*
        keyword : 검색어
        paramToken : 유튜브 API 전달할 페이지토큰
     */
    private String fetchYoutubeData(String keyword, String paramToken, String[] exclusiveChannels){
        log.debug(String.format("\n==================== 키워드로 유튜브데이터 받아오기 시작, 키워드 : %s =========================", keyword));
        // insert 순서 : 채널정보, 재생목록, 유튜브영상목록, 댓글목록
        Map<String, Object> resultMap = m_youtubeClient.getYoutubePlayListDTO(keyword, paramToken, exclusiveChannels);
        List<YoutubePlayListDTO> playLists = (List<YoutubePlayListDTO>) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PLAY_LIST);

        String nextPageToken = (String) resultMap.get(YoutubeDataV3Client.YoutubePlayListMapKey.PAGE_TOKEN);
        m_channelManager.initManager(playLists); // 재생목록에 있는 채널들의 정보를 초기화하고 재생목록의 DB 채널 id를 업데이트 해준다

        Iterator<YoutubePlayListDTO> iter = playLists.iterator();
        while(iter.hasNext()){
            // - 재생목록에 있는 모든 영상을 가져오고 감정분석으로 걸러낸다.
            // - 부정적인 댓글을 가진 영상이 있는 재생목록은 삭제한다.
            YoutubePlayListDTO playlist = iter.next();
            List<YoutubeVideoDTO> videos = m_youtubeClient.getVideoDTOListByPlayListId(playlist.playListId);
            if(!hasPositiveCommentAndLinkComments(videos)){
                iter.remove();
                continue;
            }
            playlist.setItemCount(videos.size());
            playlist.setVideos(videos);
        }
//        // TODO : 테스트용 코드
//        playLists.stream().forEach(dto -> {
//            log.debug("\n============ insert 전 재생목록 ===========");
//            log.debug(String.format("\n============ 재생목록 정보 : %s", dto.toString()));
//        });

        m_youtubeRepository.savePlayList(playLists);
        for(YoutubePlayListDTO playlistDTO : playLists){
            for(YoutubeVideoDTO video : playlistDTO.getVideos()){
                video.setYoutubePlaylistId(playlistDTO.getId());
                video.setChannelId(playlistDTO.getChannelId());
            }
            //log.debug( String.format("\n============== playlist id :  %s  ================= video list insert 시작 ==========================================", playlistDTO.playListId));
            m_youtubeRepository.saveVideoList(playlistDTO.getVideos());
            //log.debug( String.format("\n============== playlist id :  %s  ================= video list insert 끝 ==========================================", playlistDTO.playListId));
            saveCommentList(playlistDTO.getVideos());
        }
        return nextPageToken;
    }

    private void saveCommentList(List<YoutubeVideoDTO> videos){
        for(YoutubeVideoDTO videoDTO : videos){
            //log.debug("\n======= 댓글목록 저장전 videoDTO.getComments : " + videoDTO.getComments());
            for(YoutubeCommentDTO commentDTO : videoDTO.getComments()){
                //log.debug("\n======== 댓글목록 저장전 commentDTO 정보 : " + commentDTO.toString());
                commentDTO.setYoutubeId(videoDTO.getId());
            }
            m_youtubeRepository.saveCommentList(videoDTO.getComments());
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
        - 각 영상의 댓글리스트을 유튜브API로 가져온다
        - 부정적인 댓글이 있으면 바로 false리턴
        - 부정적이지 않은 댓글이 있는 영상에는 가져온 댓글리스트를 설정하고 true 리턴
     */
    private boolean hasPositiveCommentAndLinkComments(List<YoutubeVideoDTO> videos){
        for(YoutubeVideoDTO video : videos){
            List<YoutubeCommentDTO> comments = null;
            try{
                comments = m_youtubeClient.getCommentList(video.getVideoId());
//                if(!analyzeComment(comments, video)){ // 부정적인 댓글이 있다면 즉시 false 리턴 . 감정분석 API 할당량 때문에 테스트할때는 주석처리한다
//                    return false;
//                }
            }catch(LilacYoutubeAPIException le){
                log.error("댓글 감정분석중 예외 발생 : 예외가 발생한 video id = " + video.getVideoId(), le);
                video.setComments(new ArrayList<>(0));
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
}
