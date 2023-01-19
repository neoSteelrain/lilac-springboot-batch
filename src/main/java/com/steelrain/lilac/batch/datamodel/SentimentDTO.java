package com.steelrain.lilac.batch.datamodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class SentimentDTO {
    private float score;
    private float magnitude;
}
