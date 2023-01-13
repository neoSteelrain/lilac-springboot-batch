package com.steelrain.lilac.batch.youtube;

import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class YoutubeTests {

    @Autowired
    private APIConfig apiConfig;



    @Test
    public void testgGetYoutubePlayList(){
        YoutubeDataV3Client client = new YoutubeDataV3Client(apiConfig);
        SearchListResponse res = client.getYoutubePlayList("자바 Spring");

        assertThat(res != null);
    }
}
