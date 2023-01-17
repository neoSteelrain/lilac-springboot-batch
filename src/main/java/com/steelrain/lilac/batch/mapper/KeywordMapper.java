package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 미리 DB에 등록된 검색어들에 대한 myBatis 매퍼
 *
 */
@Mapper
public interface KeywordMapper {

    // tbl_subject 테이블의 myBatis 매퍼
    @Select("SELECT id, name_kr, name_eng FROM tbl_subject WHERE is_active=1")
    @Results(id="KeywordSubjectMap", value={
            @Result(property = "id", column = "id"),
            @Result(property = "nameKr", column = "name_kr"),
            @Result(property = "nameEng", column = "name_eng")
    })
    public List<KeywordSubjectDTO> getSubjectList();

    // tbl_license 테이블의 myBatis 매퍼
    @Select("SELECT code, name FROM tbl_license WHERE is_active=1")
    @Results(id="KeywordLicenseMap", value = {
            @Result(property = "code", column = "code"),
            @Result(property = "name", column = "name")
    })
    public List<KeywordLicenseDTO> getLicenseList();
}
