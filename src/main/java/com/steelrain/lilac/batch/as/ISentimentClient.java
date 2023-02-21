package com.steelrain.lilac.batch.as;

import com.steelrain.lilac.batch.datamodel.SentimentDTO;

public interface ISentimentClient {
    SentimentDTO analyzeComment(String comment);
}
