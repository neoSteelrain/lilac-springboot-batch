package com.steelrain.lilac.batch.exception;

import lombok.Getter;

@Getter
public class LilacYoutubeAPIException extends LilacBatchException{

    private String m_keyword;
    public LilacYoutubeAPIException(String msg){
        super(msg);
    }

    public LilacYoutubeAPIException(String msg, String keyword){
        super(msg);
        this.m_keyword = keyword;
    }

    public LilacYoutubeAPIException(String msg, Exception e){
        super(msg, e);
    }

    public LilacYoutubeAPIException(String msg, Exception e, String keyword){
        super(msg, e);
        this.m_keyword = keyword;
    }
}
