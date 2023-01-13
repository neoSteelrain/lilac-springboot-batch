package com.steelrain.lilac.batch.exception;

public class LilacYoutubeAPIException extends LilacBatchException{

    public LilacYoutubeAPIException(String msg){
        super(msg);
    }

    public LilacYoutubeAPIException(String msg, Exception e){
        super(msg, e);
    }
}
