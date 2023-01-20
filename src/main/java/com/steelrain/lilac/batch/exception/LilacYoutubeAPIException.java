package com.steelrain.lilac.batch.exception;

import lombok.Getter;

@Getter
public class LilacYoutubeAPIException extends LilacBatchException{

    private String m_param;
    public LilacYoutubeAPIException(String msg){
        super(msg);
    }

    public LilacYoutubeAPIException(String msg, String param){
        super(msg);
        this.m_param = param;
    }

    public LilacYoutubeAPIException(String msg, Exception e){
        super(msg, e);
    }

    public LilacYoutubeAPIException(String msg, Exception e, String param){
        super(msg, e);
        this.m_param = param;
    }
}
