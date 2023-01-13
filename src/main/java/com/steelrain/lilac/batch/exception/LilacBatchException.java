package com.steelrain.lilac.batch.exception;

public class LilacBatchException extends RuntimeException{
    public LilacBatchException(String msg){
        super(msg);
    }

    public LilacBatchException(String msg, Exception e){
        super(msg, e);
    }
}
