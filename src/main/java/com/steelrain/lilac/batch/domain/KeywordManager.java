package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import com.steelrain.lilac.batch.datamodel.LicenseBatchResultDTO;
import com.steelrain.lilac.batch.datamodel.SubjectBatchResultDTO;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DB에 검색어로 등록된 키워드를 관리하는 클래스 
 */
@Component
public class KeywordManager {
    private final KeywordMapper m_keywordMapper;
    private List<KeywordSubjectDTO> m_subjectDTOList;
    private List<KeywordLicenseDTO> m_licenseDTOList;

    public KeywordManager(KeywordMapper keywordMapper){
        this.m_keywordMapper = keywordMapper;

        m_subjectDTOList = m_keywordMapper.getSubjectList();
        m_licenseDTOList = m_keywordMapper.getLicenseList();
    }

    public List<KeywordSubjectDTO> getSubjectList(){
        return this.m_subjectDTOList;
    }

    public List<KeywordLicenseDTO> getLicenseList(){
        return this.m_licenseDTOList;
    }

    public boolean updateSubjectPageToken(SubjectBatchResultDTO batchResultDTO){
        return m_keywordMapper.udpateSubjectPageToken(batchResultDTO) > 0;
    }

    public boolean updateLicensePageToken(LicenseBatchResultDTO batchResultDTO){
        return m_keywordMapper.updateLicensePageToken(batchResultDTO) > 0;
    }
}
