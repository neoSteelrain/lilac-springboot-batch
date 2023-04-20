package com.steelrain.lilac.batch.domain;

public interface IYoutubeAgent {
    String fetchYoutubeData(String keyword, String paramToken, Integer licenseId, Integer subjectId, String[] exclusiveChannels);
}
