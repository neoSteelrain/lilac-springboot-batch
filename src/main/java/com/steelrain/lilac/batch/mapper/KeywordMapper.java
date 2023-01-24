package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import com.steelrain.lilac.batch.datamodel.LicenseBatchResultDTO;
import com.steelrain.lilac.batch.datamodel.SubjectBatchResultDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 미리 DB에 등록된 검색어들에 대한 myBatis 매퍼
 *
 */
@Mapper
public interface KeywordMapper {

    // tbl_subject 테이블의 myBatis 매퍼
    @Select("SELECT id,name,key_word,page_token,update_time FROM tbl_subject WHERE is_active=1")
    @Results(id="KeywordSubjectMap", value={
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "keyWord", column = "key_word"),
            @Result(property = "pageToken", column = "page_token"),
            @Result(property = "updateTime", column = "update_time")
    })
    public List<KeywordSubjectDTO> getSubjectList();

    // tbl_license 테이블의 myBatis 매퍼
    @Select("SELECT id,code,name,key_word,page_token,update_time FROM tbl_license WHERE is_active=1")
    @Results(id="KeywordLicenseMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "code", column = "code"),
            @Result(property = "name", column = "name"),
            @Result(property = "keyWord", column = "key_word"),
            @Result(property = "pageToken", column = "page_token"),
            @Result(property = "updateTime", column = "update_time")
    })
    public List<KeywordLicenseDTO> getLicenseList();

    @Update("UPDATE tbl_subject SET page_token=#{pageToken},update_time=now() WHERE id=#{id}")
    public int udpateSubjectPageToken(SubjectBatchResultDTO batchResultDTO);

    @Update("UPDATE tbl_license SET page_token=#{pageToken},update_time=now() WHERE id=#{id}")
    public int updateLicensePageToken(LicenseBatchResultDTO batchResultDTO);
}
