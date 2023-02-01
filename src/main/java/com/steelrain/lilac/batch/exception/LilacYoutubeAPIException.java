package com.steelrain.lilac.batch.exception;

import lombok.Getter;

@Getter
public class LilacYoutubeAPIException extends LilacBatchException{

    private String param;
    public LilacYoutubeAPIException(String msg){
        super(msg);
    }

    public LilacYoutubeAPIException(String msg, String param){
        super(msg);
        this.param = param;
    }

    public LilacYoutubeAPIException(String msg, Exception e){
        super(msg, e);
    }

    public LilacYoutubeAPIException(String msg, Exception e, String param){
        super(msg, e);
        this.param = param;
    }
}
