package com.steelrain.lilac.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class APIConfig {
    private String youtubeKey;

    // 감정분석의 긍정점수(score)값의 임계치
    private float threshold;
    private long commentCount;

    public APIConfig(String youtubeKey, String threshold, long commentCount){
        this.youtubeKey = youtubeKey;
        this.threshold = Float.valueOf(threshold).floatValue();
        this.commentCount = commentCount;
    }
}
