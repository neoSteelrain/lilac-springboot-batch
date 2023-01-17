package com.steelrain.lilac.batch.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;




public class YoutubeTests {

    @Autowired
    private APIConfig apiConfig;


    @Test
    public void testGetYoutubePlayList(){
        IYoutubeClient youtubeClient = new YoutubeDataV3Client(this.apiConfig);
        SearchListResponse res = youtubeClient.getYoutubePlayList("정보처리기사");

        assertThat(res != null);

        testTEEEE(res);
        //searchListResponseToJsonFile(res);
    }

    /*@Test
    public void testGetYoutubePlayListByMock(){
        IYoutubeClient client = new YoutubeClientMock();
        List<YoutubePlayListDTO> result = client.getYoutubePlayListDTO("정보처리기사");

        assertThat(result != null);

        System.out.println("result 1 : " + result.get(0).toString());
    }*/

    /*@Test
    public void testGetVideoListByPlayListId(){
        IYoutubeClient youtubeClient = new YoutubeDataV3Client(this.apiConfig);
        PlaylistItemListResponse response = youtubeClient.getVideoListByPlayListId("PL6i7rGeEmTvqEjTJF3PJR4a1N9KTPpfw0");

        assertThat(response != null);

        getVideoListByPlayListIdToJsonFile(response);
    }*/

    private void getVideoListByPlayListIdToJsonFile(PlaylistItemListResponse response){
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File("C:\\lilac-youtube_jsons\\PlaylistItemListResponse.json"),response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void searchListResponseToJsonFile(SearchListResponse response){
        ObjectMapper om = new ObjectMapper();
        try {
            om.writeValue(new File("C:\\lilac-youtube_jsons\\SearchListResponse.json"), response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTEEEE(SearchListResponse response){
        try{
            String tmp = response.getFactory().toString();
            System.out.println("tmp : " + tmp);

            String tmp2 = JacksonFactory.getDefaultInstance().toString(response);
            System.out.println("tmp2 : " + tmp2);
            SearchListResponse res = JacksonFactory.getDefaultInstance().fromString(tmp2, SearchListResponse.class);
            System.out.println("res.toPrettyString() : " + res.toPrettyString());

            // C:\lilac-youtube_jsons\SearchListResponse.json
            /*String tmp = FileCopyUtils.copyToString(new FileReader("C:\\lilac-youtube_jsons\\SearchListResponse.json"));
            System.out.println(tmp);
            SearchListResponse res2 = JacksonFactory.getDefaultInstance().fromString(tmp, SearchListResponse.class);
            System.out.println("res2.toPrettyString() : " + res2.toPrettyString());*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
