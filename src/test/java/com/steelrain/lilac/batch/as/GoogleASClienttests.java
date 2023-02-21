package com.steelrain.lilac.batch.as;



import com.steelrain.lilac.batch.datamodel.SentimentDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class GoogleASClienttests {

    @Test
    public void testAnalyizeComment(){
        ISentimentClient client = new GoogleASClient();
        SentimentDTO res = client.analyzeComment("야이 이딴 거지같은 프로그램짜고 부끄럽지도 않냐 다 때려치워라");
        assertThat( res != null);
        System.out.printf("res : socre = %f , magnitude = %f \n", res.getScore(), res.getMagnitude());
    }
}
