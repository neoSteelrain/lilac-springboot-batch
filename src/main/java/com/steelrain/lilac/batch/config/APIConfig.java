package com.steelrain.lilac.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class APIConfig {
    private String youtubeKey;

    public APIConfig(String youtubeKey){
        this.youtubeKey = youtubeKey;
    }
}
