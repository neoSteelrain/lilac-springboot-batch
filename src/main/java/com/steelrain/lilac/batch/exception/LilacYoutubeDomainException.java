package com.steelrain.lilac.batch.exception;

public class LilacYoutubeDomainException extends LilacBatchException{
    public LilacYoutubeDomainException(String msg){
        super(msg);
    }

    public LilacYoutubeDomainException(String msg, Exception e){
        super(msg, e);
    }
}
