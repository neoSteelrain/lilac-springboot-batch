package com.steelrain.lilac.batch.datamodel;

import lombok.Getter;
import lombok.Setter;

/**
 * 검색주제어를 나타내는 DTO
 * tbl_subject 테이블 매핑
 */
@Getter
@Setter
public class KeywordSubjectDTO {

    private Long id;
    private String nameKr;
    private String nameEng;
}
