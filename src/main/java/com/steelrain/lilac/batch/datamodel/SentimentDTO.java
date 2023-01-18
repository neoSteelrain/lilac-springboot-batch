package com.steelrain.lilac.batch.datamodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SentimentDTO {
    private float score;
    private float magnitude;
}
