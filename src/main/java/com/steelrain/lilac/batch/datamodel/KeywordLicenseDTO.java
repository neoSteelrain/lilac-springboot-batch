package com.steelrain.lilac.batch.datamodel;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class KeywordLicenseDTO{

    private Integer id;
    private Integer code;
    private String name;
    private String keyWord;
    private String pageToken;
    private Timestamp updateTime;
}
