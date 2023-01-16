package com.steelrain.lilac.batch.youtube;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class YoutubeTests {

    @Autowired
    private APIConfig apiConfig;


    @Test
    public void testGetYoutubePlayList(){
        IYoutubeClient youtubeClient = new YoutubeDataV3Client(this.apiConfig);
        SearchListResponse res = youtubeClient.getYoutubePlayList("정보처리기사");

        assertThat(res != null);

        searchListResponseToJsonFile(res);
    }


    private void searchListResponseToJsonFile(SearchListResponse response){
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File("C:\\lilac-youtube_jsons\\SearchListResponse.json"),response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
