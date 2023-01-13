package com.steelrain.lilac.batch.youtube;

import com.google.api.services.youtube.model.SearchListResponse;

public interface IYoutubeClient {
    SearchListResponse getYoutubePlayList(String keyword);
}
