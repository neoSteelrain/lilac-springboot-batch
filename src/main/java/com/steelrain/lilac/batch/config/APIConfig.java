package com.steelrain.lilac.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class APIConfig {
    private String youtubeKey;

    // 감정분석의 긍정점수(score)값의 임계치
    private float threshold;

    public APIConfig(String youtubeKey, String threshold){
        this.youtubeKey = youtubeKey;
        this.threshold = Float.valueOf(threshold);
    }
}
