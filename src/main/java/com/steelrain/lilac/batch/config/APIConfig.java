package com.steelrain.lilac.batch.config;

import com.google.api.client.util.DateTime;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class APIConfig {
    private String youtubeKey;

    private Boolean sentimentActive;
    // 감정분석의 긍정점수(score)값의 임계치
    private Float threshold;
    private Long commentCount;
    private Long playlistFetchSize;
    private String[] exclusiveChannels;
    // DateTime.parseRfc3339
    private DateTime searchPublishDate;


    public APIConfig(String youtubeKey, Boolean sentimentActive, String threshold, String commentCount, String playlistFetchSize, String exclusiveChannels, String searchPublishDate){
        this.youtubeKey = youtubeKey;
        this.sentimentActive = sentimentActive;
        this.threshold = Float.valueOf(threshold).floatValue();
        this.commentCount = Long.valueOf(commentCount);
        this.playlistFetchSize = Long.valueOf(playlistFetchSize);
        this.exclusiveChannels = StringUtils.containsWhitespace(",") ? StringUtils.split(exclusiveChannels, ",") : new String[] {exclusiveChannels};
        this.searchPublishDate = DateTime.parseRfc3339(searchPublishDate);
    }
}
