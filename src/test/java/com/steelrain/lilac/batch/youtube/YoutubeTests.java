package com.steelrain.lilac.batch.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.*;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;





@SpringBootTest
public class YoutubeTests {

    @Autowired
    private APIConfig apiConfig;

    @Test
    public void testGetYoutubePlayList() throws IOException {
        YoutubeDataV3Client youtubeClient = new YoutubeDataV3Client(this.apiConfig);
        SearchListResponse res = youtubeClient.getSearchListResponse("정보처리기사");

        String tmp2 = JacksonFactory.getDefaultInstance().toString(res);
        System.out.println("========== tmp2 : )" + tmp2);
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

    @Test
    public void testGetVideoDTOListByPlayListId(){
        // PL6i7rGeEmTvqEjTJF3PJR4a1N9KTPpfw0 정치기
        // PLW2UjW795-f5JPTsYHGAawAck9cQRw5TD 자바
        IYoutubeClient youtubeClient = new YoutubeDataV3Client(this.apiConfig);
        List<YoutubeVideoDTO> res = youtubeClient.getVideoDTOListByPlayListId("PLW2UjW795-f5JPTsYHGAawAck9cQRw5TD");

        assertThat(res != null && res.size() > 0);

        System.out.println("cnt : " + res.size());
        res.stream().forEach(dto -> {
            System.out.println(dto.toString());
        });
    }

    @Test
    public void testCommentThreadsAPI() {
        String resultStr = "{\n" +
                "  \"kind\": \"youtube#commentThreadListResponse\",\n" +
                "  \"etag\": \"S0sWCdVZqbI6g9HsmKUPgcOTxtM\",\n" +
                "  \"pageInfo\": {\n" +
                "    \"totalResults\": 1,\n" +
                "    \"resultsPerPage\": 20\n" +
                "  },\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"kind\": \"youtube#commentThread\",\n" +
                "      \"etag\": \"atADMN53TGPF_26TlgZaJQmUw08\",\n" +
                "      \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "      \"snippet\": {\n" +
                "        \"videoId\": \"yChF_Qnwstg\",\n" +
                "        \"topLevelComment\": {\n" +
                "          \"kind\": \"youtube#comment\",\n" +
                "          \"etag\": \"s0_zyYsSxlXqa4oLJj_xWp6bGLs\",\n" +
                "          \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "          \"snippet\": {\n" +
                "            \"videoId\": \"yChF_Qnwstg\",\n" +
                "            \"textDisplay\": \"궁금한게 생겨서 질문 합니다,\\n밑에 나온 과정부분에서 코딩하고나서 시험으로 넘어가면 시험은 테스트를 하는부분이라 문제가 나올수 있는건데 코딩으로 넘어가지 못하면 시험부분에서 고치지 못한다는 말인가요?\",\n" +
                "            \"textOriginal\": \"궁금한게 생겨서 질문 합니다,\\n밑에 나온 과정부분에서 코딩하고나서 시험으로 넘어가면 시험은 테스트를 하는부분이라 문제가 나올수 있는건데 코딩으로 넘어가지 못하면 시험부분에서 고치지 못한다는 말인가요?\",\n" +
                "            \"authorDisplayName\": \"바람여행\",\n" +
                "            \"authorProfileImageUrl\": \"https://yt3.ggpht.com/ytc/AMLnZu-kdMA-j78V_zxwBULi_XrlLaE4r4HI0V_3DNqn=s48-c-k-c0x00ffffff-no-rj\",\n" +
                "            \"authorChannelUrl\": \"http://www.youtube.com/channel/UCkjH6KZ1PXsOCcqlCImoDVA\",\n" +
                "            \"authorChannelId\": {\n" +
                "              \"value\": \"UCkjH6KZ1PXsOCcqlCImoDVA\"\n" +
                "            },\n" +
                "            \"canRate\": true,\n" +
                "            \"viewerRating\": \"none\",\n" +
                "            \"likeCount\": 0,\n" +
                "            \"publishedAt\": \"2023-01-09T08:35:39Z\",\n" +
                "            \"updatedAt\": \"2023-01-09T08:35:39Z\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"canReply\": true,\n" +
                "        \"totalReplyCount\": 1,\n" +
                "        \"isPublic\": true\n" +
                "      },\n" +
                "      \"replies\": {\n" +
                "        \"comments\": [\n" +
                "          {\n" +
                "            \"kind\": \"youtube#comment\",\n" +
                "            \"etag\": \"O_MRfEEIWfmBLwPBtStFfYxPHKM\",\n" +
                "            \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg.9keX_KC7m7s9knzi0k01cu\",\n" +
                "            \"snippet\": {\n" +
                "              \"videoId\": \"yChF_Qnwstg\",\n" +
                "              \"textDisplay\": \"폭포수 모형의 경우 이전 단계로 돌아갈 수 없다는 것을 전제로 하고 있으나, 아예 불가능한 것은 아닙니다. 단지 힘들고 어렵기 때문에 최대한 해당 과정에서 오류가 없도록 마무리 하는 것이죠.\",\n" +
                "              \"textOriginal\": \"폭포수 모형의 경우 이전 단계로 돌아갈 수 없다는 것을 전제로 하고 있으나, 아예 불가능한 것은 아닙니다. 단지 힘들고 어렵기 때문에 최대한 해당 과정에서 오류가 없도록 마무리 하는 것이죠.\",\n" +
                "              \"parentId\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "              \"authorDisplayName\": \"길벗시나공 IT\",\n" +
                "              \"authorProfileImageUrl\": \"https://yt3.ggpht.com/7qH2U6qg2JRyKKIzi5Umkwy-Iixir7vEQv77iUpzIQF2RXlspTh-G2NnNlSzOv-7Ut6W6j_eCQ=s48-c-k-c0x00ffffff-no-rj\",\n" +
                "              \"authorChannelUrl\": \"http://www.youtube.com/channel/UCPb3m8raQQATP-nlPwDRRXA\",\n" +
                "              \"authorChannelId\": {\n" +
                "                \"value\": \"UCPb3m8raQQATP-nlPwDRRXA\"\n" +
                "              },\n" +
                "              \"canRate\": true,\n" +
                "              \"viewerRating\": \"none\",\n" +
                "              \"likeCount\": 0,\n" +
                "              \"publishedAt\": \"2023-01-13T00:43:25Z\",\n" +
                "              \"updatedAt\": \"2023-01-13T00:43:25Z\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        // SearchListResponse res = JacksonFactory.getDefaultInstance().fromString(tmp2, SearchListResponse.class);
        try {
            CommentThreadListResponse res = JacksonFactory.getDefaultInstance().fromString(resultStr, CommentThreadListResponse.class);
            List<CommentThread> items = res.getItems();
            CommentThread thread = items.get(0);
            System.out.println("thread.getSnippet() : " + (thread.getSnippet() == null && thread.getSnippet().getTopLevelComment() == null &&
                        thread.getSnippet().getTopLevelComment().getSnippet() == null));
            assertThat(thread.getSnippet() == null && thread.getSnippet().getTopLevelComment() == null &&
                    thread.getSnippet().getTopLevelComment().getSnippet() == null).isTrue();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateCommentDTO(){
        String resultStr = "{\n" +
                "  \"kind\": \"youtube#commentThreadListResponse\",\n" +
                "  \"etag\": \"S0sWCdVZqbI6g9HsmKUPgcOTxtM\",\n" +
                "  \"pageInfo\": {\n" +
                "    \"totalResults\": 1,\n" +
                "    \"resultsPerPage\": 20\n" +
                "  },\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"kind\": \"youtube#commentThread\",\n" +
                "      \"etag\": \"atADMN53TGPF_26TlgZaJQmUw08\",\n" +
                "      \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "      \"snippet\": {\n" +
                "        \"videoId\": \"yChF_Qnwstg\",\n" +
                "        \"topLevelComment\": {\n" +
                "          \"kind\": \"youtube#comment\",\n" +
                "          \"etag\": \"s0_zyYsSxlXqa4oLJj_xWp6bGLs\",\n" +
                "          \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "          \"snippet\": {\n" +
                "            \"videoId\": \"yChF_Qnwstg\",\n" +
                "            \"textDisplay\": \"궁금한게 생겨서 질문 합니다,\\n밑에 나온 과정부분에서 코딩하고나서 시험으로 넘어가면 시험은 테스트를 하는부분이라 문제가 나올수 있는건데 코딩으로 넘어가지 못하면 시험부분에서 고치지 못한다는 말인가요?\",\n" +
                "            \"textOriginal\": \"궁금한게 생겨서 질문 합니다,\\n밑에 나온 과정부분에서 코딩하고나서 시험으로 넘어가면 시험은 테스트를 하는부분이라 문제가 나올수 있는건데 코딩으로 넘어가지 못하면 시험부분에서 고치지 못한다는 말인가요?\",\n" +
                "            \"authorDisplayName\": \"바람여행\",\n" +
                "            \"authorProfileImageUrl\": \"https://yt3.ggpht.com/ytc/AMLnZu-kdMA-j78V_zxwBULi_XrlLaE4r4HI0V_3DNqn=s48-c-k-c0x00ffffff-no-rj\",\n" +
                "            \"authorChannelUrl\": \"http://www.youtube.com/channel/UCkjH6KZ1PXsOCcqlCImoDVA\",\n" +
                "            \"authorChannelId\": {\n" +
                "              \"value\": \"UCkjH6KZ1PXsOCcqlCImoDVA\"\n" +
                "            },\n" +
                "            \"canRate\": true,\n" +
                "            \"viewerRating\": \"none\",\n" +
                "            \"likeCount\": 0,\n" +
                "            \"publishedAt\": \"2023-01-09T08:35:39Z\",\n" +
                "            \"updatedAt\": \"2023-01-09T08:35:39Z\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"canReply\": true,\n" +
                "        \"totalReplyCount\": 1,\n" +
                "        \"isPublic\": true\n" +
                "      },\n" +
                "      \"replies\": {\n" +
                "        \"comments\": [\n" +
                "          {\n" +
                "            \"kind\": \"youtube#comment\",\n" +
                "            \"etag\": \"O_MRfEEIWfmBLwPBtStFfYxPHKM\",\n" +
                "            \"id\": \"UgyuSHf-XIkXeSI24xx4AaABAg.9keX_KC7m7s9knzi0k01cu\",\n" +
                "            \"snippet\": {\n" +
                "              \"videoId\": \"yChF_Qnwstg\",\n" +
                "              \"textDisplay\": \"폭포수 모형의 경우 이전 단계로 돌아갈 수 없다는 것을 전제로 하고 있으나, 아예 불가능한 것은 아닙니다. 단지 힘들고 어렵기 때문에 최대한 해당 과정에서 오류가 없도록 마무리 하는 것이죠.\",\n" +
                "              \"textOriginal\": \"폭포수 모형의 경우 이전 단계로 돌아갈 수 없다는 것을 전제로 하고 있으나, 아예 불가능한 것은 아닙니다. 단지 힘들고 어렵기 때문에 최대한 해당 과정에서 오류가 없도록 마무리 하는 것이죠.\",\n" +
                "              \"parentId\": \"UgyuSHf-XIkXeSI24xx4AaABAg\",\n" +
                "              \"authorDisplayName\": \"길벗시나공 IT\",\n" +
                "              \"authorProfileImageUrl\": \"https://yt3.ggpht.com/7qH2U6qg2JRyKKIzi5Umkwy-Iixir7vEQv77iUpzIQF2RXlspTh-G2NnNlSzOv-7Ut6W6j_eCQ=s48-c-k-c0x00ffffff-no-rj\",\n" +
                "              \"authorChannelUrl\": \"http://www.youtube.com/channel/UCPb3m8raQQATP-nlPwDRRXA\",\n" +
                "              \"authorChannelId\": {\n" +
                "                \"value\": \"UCPb3m8raQQATP-nlPwDRRXA\"\n" +
                "              },\n" +
                "              \"canRate\": true,\n" +
                "              \"viewerRating\": \"none\",\n" +
                "              \"likeCount\": 0,\n" +
                "              \"publishedAt\": \"2023-01-13T00:43:25Z\",\n" +
                "              \"updatedAt\": \"2023-01-13T00:43:25Z\"\n" +
                "            }\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        CommentThreadListResponse res = null;
        try {
            res = JacksonFactory.getDefaultInstance().fromString(resultStr, CommentThreadListResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<CommentThread> items = res.getItems();
        CommentThread thread = items.get(0);
        if(thread.getSnippet() == null && thread.getSnippet().getTopLevelComment() == null &&
                thread.getSnippet().getTopLevelComment().getSnippet() == null){
            System.out.println("TTTTTTTTTTTTTT");;
        }
        YoutubeCommentDTO dto = YoutubeCommentDTO.builder()
                .textOriginal(thread.getSnippet().getTopLevelComment().getSnippet().getTextOriginal())
                .textDisplay(thread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay())
                .authorDisplayName(thread.getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName())
                .totalReplyCount(thread.getSnippet().getTotalReplyCount())
                .publishDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getPublishedAt().getValue()))
                .updateDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getUpdatedAt().getValue()))
                .build();

        System.out.println("=======================");
        System.out.println(dto.toString());
        System.out.println("=======================");
    }
}
