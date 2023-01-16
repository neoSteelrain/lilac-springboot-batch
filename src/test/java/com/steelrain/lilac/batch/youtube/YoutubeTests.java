package com.steelrain.lilac.batch.youtube;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import io.micrometer.core.instrument.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class YoutubeTests {

    @Autowired
    private APIConfig apiConfig;



    @Test
    public void testgGetYoutubePlayList(){
        YoutubeDataV3Client client = new YoutubeDataV3Client(apiConfig);
        SearchListResponse res = client.getYoutubePlayList("정보처리기사 2022");

        searchListResponseToJsonFile(res);

        assertThat(res != null);
    }

    @Test
    public void testGetYoutubePlayListV2(){
        YoutubeClientV2 v2 = new YoutubeClientV2(apiConfig);
        SearchListResponse res = v2.getYoutubePlayList("정보처리기사 2023");
        searchListResponseToJsonFile(res);
    }

   /* @Test
    public void testGoggleDateTime(){
        YoutubeClientV2 v2 = new YoutubeClientV2(apiConfig);
        DateTime dt = v2.getDateTimeTest();
        String tmp = dt.toStringRfc3339();

        assertThat(!StringUtils.isEmpty(tmp));

        System.out.println("==============================");
        System.out.println("tmp : " + tmp);
        System.out.println("==============================");
    }*/



    private void searchListResponseToJsonFile(SearchListResponse response){
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File("C:\\lilac-youtube_jsons\\SearchListResponse.json"),response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
