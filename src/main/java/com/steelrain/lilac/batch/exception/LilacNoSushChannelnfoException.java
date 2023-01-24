package com.steelrain.lilac.batch.exception;

import java.rmi.server.ExportException;

public class LilacNoSushChannelnfoException extends LilacBatchException{
    public LilacNoSushChannelnfoException(String msg){
        super(msg);
    }

    public LilacNoSushChannelnfoException(String msg, Exception e){
        super(msg, e);
    }
}
