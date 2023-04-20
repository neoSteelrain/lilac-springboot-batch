package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import com.steelrain.lilac.batch.datamodel.LicenseBatchResultDTO;
import com.steelrain.lilac.batch.datamodel.SubjectBatchResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
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

    private final APIConfig m_apiConfig;
    private final KeywordManager m_keywordManager;
    private final Map<String, IYoutubeAgent> m_youtubeAgentMap;
    private IYoutubeAgent m_youtubeAgent;

    @Autowired
    public YoutubeManager(APIConfig apiConfig, KeywordManager keywordManager, Map<String, IYoutubeAgent> agentMap) {
        // TODO 같은 인터페이스를 구현한 2개의 빈을 동시에 가져오는 좀더 나은 방법을 찾아야 한다  
        this.m_apiConfig = apiConfig;
        this.m_keywordManager = keywordManager;
        this.m_youtubeAgentMap = agentMap;

        initYoutubeAgent();
    }

    private void initYoutubeAgent(){
        // TODO 빈이름을 하드코딩으로 가져오고 있어서, 좀더 나은 방법을 찾아야 한다
        m_youtubeAgent = m_apiConfig.getSentimentActive() ? m_youtubeAgentMap.get("youtubeASAgent") : m_youtubeAgentMap.get("youtubeDefaultAgent");
    }

    @Transactional
    public void doYoutubeBatch(){
        log.debug("현재 실행되는 YoutubeAgent : {}", m_youtubeAgent.getClass().getName());
        doSubjectBatch();
        doLicenseBatch();
    }

    private void doLicenseBatch() {
        List<KeywordLicenseDTO> licenseList = m_keywordManager.getLicenseList();
        for (KeywordLicenseDTO licenseDTO : licenseList) {
            String nextPageToken = m_youtubeAgent.fetchYoutubeData(licenseDTO.getKeyWord(), licenseDTO.getPageToken(), licenseDTO.getId(), null, m_apiConfig.getExclusiveChannels());
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
            String pageToken = m_youtubeAgent.fetchYoutubeData(subjectDTO.getKeyWord(), subjectDTO.getPageToken(), null, subjectDTO.getId(), null);
            SubjectBatchResultDTO batchResultDTO = SubjectBatchResultDTO.builder()
                    .id(subjectDTO.getId())
                    .pageToken(pageToken)
                    .build();
            m_keywordManager.updateSubjectPageToken(batchResultDTO);
        }
    }
}
