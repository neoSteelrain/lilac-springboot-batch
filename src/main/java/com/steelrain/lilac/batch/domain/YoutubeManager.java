package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.as.ISentimentClient;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.*;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import com.steelrain.lilac.batch.youtube.YoutubeDataV3Client;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
@Component
public class YoutubeManager {

    private final KeywordMapper m_subjectMapper;
    private final IYoutubeClient m_youYoutubeClient;
    private final ISentimentClient m_sentimentClient;
    private final APIConfig m_apiConfig;
    private final CommentByteCounter m_commentByteCounter;

    public YoutubeManager(KeywordMapper subjectMapper,
                          IYoutubeClient youtubeClient,
                          ISentimentClient sentimentClient,
                          APIConfig apiConfig,
                          CommentByteCounter commentByteCounter){
        this.m_subjectMapper = subjectMapper;
        this.m_youYoutubeClient = youtubeClient;
        this.m_sentimentClient = sentimentClient;
        this.m_apiConfig = apiConfig;
        this.m_commentByteCounter = commentByteCounter;
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

    public void dispatchYoutube(){
        List<KeywordSubjectDTO> subjectList = getSubjectList();
        List<KeywordLicenseDTO> licenseList = getLicenseList();

        // "정보처리기사" 키워드로 테스트
        String keyword = "정보처리기사";
        List<YoutubePlayListDTO> playListDTOS = m_youYoutubeClient.getYoutubePlayListDTO(keyword);
        for(YoutubePlayListDTO dto : playListDTOS){
            // 재생목록에 있는 모든 영상을 가져오고 감정분석으로 걸러서 새로운 영상리스트를 만든다.
            List<YoutubeVideoDTO> filteredVideos = filterVideoList(m_youYoutubeClient.getVideoDTOListByPlayListId(dto.playListId));
            // TODO : 걸러진 영상리스트 및 재생목록정보를 DB에 넣는다.

        }
    }

    // 영상들의 댓글리스트를 가져오고 감정분석을 하여 긍정수치가 임계치를 넘은 댓글을 가진 유튜브영상들의 리스트를 새로만들어 반환한다.
    private List<YoutubeVideoDTO> filterVideoList(List<YoutubeVideoDTO> videos){
        List<YoutubeVideoDTO> resultList = new ArrayList<>(videos.size());
        for(YoutubeVideoDTO video : videos){
            List<YoutubeCommentDTO> comments = m_youYoutubeClient.getCommentList(video.getVideoId());
            if(analyzeComment(comments)){
                resultList.add(video);
            }
        }
        return resultList;
    }

    // 댓글리스트의 댓글들을 1000바이트
    private boolean analyzeComment(List<YoutubeCommentDTO> comments){
        String assembledComment = assembleComment(comments);
        SentimentDTO sentimentDTO = m_sentimentClient.analyizeComment(assembledComment);
        return sentimentDTO == null ? false : sentimentDTO.getScore() >= m_apiConfig.getThreshold(); // 긍정수치가 임계치보다 높으면 true 아니면 false
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
