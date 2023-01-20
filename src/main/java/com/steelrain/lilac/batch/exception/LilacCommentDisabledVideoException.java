package com.steelrain.lilac.batch.exception;

public class LilacCommentDisabledVideoException extends LilacYoutubeAPIException{
    public LilacCommentDisabledVideoException(String msg){
        super(msg);
    }

    public LilacCommentDisabledVideoException(String msg, String param){
        super(msg, param);
    }

    public LilacCommentDisabledVideoException(String msg, Exception e, String param){
        super(msg, e, param);
    }
}
