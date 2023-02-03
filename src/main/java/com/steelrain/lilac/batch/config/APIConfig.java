package com.steelrain.lilac.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

@Getter
public class APIConfig {
    private String youtubeKey;

    // 감정분석의 긍정점수(score)값의 임계치
    private Float threshold;
    private Long commentCount;
    private Long playlistFetchSize;
    private String[] exclusiveChannels;

    public APIConfig(String youtubeKey, String threshold, String commentCount, String playlistFetchSize, String exclusiveChannels){
        this.youtubeKey = youtubeKey;
        this.threshold = Float.valueOf(threshold).floatValue();
        this.commentCount = Long.valueOf(commentCount);
        this.playlistFetchSize = Long.valueOf(playlistFetchSize);
        this.exclusiveChannels = StringUtils.containsWhitespace(",") ? StringUtils.split(exclusiveChannels, ",") : new String[] {exclusiveChannels};
    }
}
