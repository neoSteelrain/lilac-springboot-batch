package com.steelrain.lilac.batch.exception;

public class LilacGoogleASException extends LilacBatchException{
    private String m_comment;

    public LilacGoogleASException(String msg, String comment){
        super(msg);
        this.m_comment = comment;
    }

    public LilacGoogleASException(String msg, Exception e, String comment) {
        super(msg, e);
        this.m_comment = comment;
    }
}
