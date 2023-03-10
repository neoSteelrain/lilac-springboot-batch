package com.steelrain.lilac.batch.repository;


import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import io.micrometer.core.annotation.TimedSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
public class YoutubeMyBatisRepositoryTests {

    @Autowired
    private IYoutubeRepository m_youtubeRepository;

    @Autowired
    private IYoutubeClient m_youtubeClient;

    @Test
    public void testSavePlayList(){
        List<YoutubePlayListDTO> list = new ArrayList<>();
        YoutubePlayListDTO dto = new YoutubePlayListDTO();
        dto.setPlayListId("aaaaaaaaaa");
        dto.setTitle("ttttttttt");
        dto.setPublishDate(new Timestamp(System.currentTimeMillis()));
        dto.setThumbnailMedium("mmmmmmmmmmmm");
        dto.setThumbnailHigh("hhhhhhhhhhhhhhh");
        dto.setItemCount(100);
        dto.setChannelId(2L);
        list.add(dto);

        int cnt = m_youtubeRepository.savePlayList(list);

        assertThat(cnt > 0);
    }

    @Test
    public void testSaveVideoList(){
        List<YoutubeVideoDTO> list = new ArrayList<>();
        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setChannelId(2L);
        dto.setYoutubePlaylistId(52L);
        dto.setVideoId("vvvvvvvvvv");
        dto.setTitle("tttttttttt");
        dto.setDescription("dddddddddddddd");
        dto.setPublishDate(new Timestamp(System.currentTimeMillis()));
        dto.setThumbnailMedium("mmmmmmmmmmm");
        dto.setThumbnailHigh("hhhhhhhhhhhhh");
        dto.setViewCount(333333L);
        dto.setSearchCount(444444);
        dto.setPlaylistId("ppppppppppp");
        dto.setLikeCount(55555L);
        dto.setFavoriteCount(6666L);
        dto.setCommentCount(77777L);
        dto.setDuration("ddddd");
        dto.setScore(0.2f);
        dto.setMagnitude(1.2f);
        list.add(dto);
        int cnt = m_youtubeRepository.saveVideoList(list);
        assertThat(cnt > 0);
    }

    @Test
    //@Transactional
    public void testSaveChannelInfo(){
        YoutubeChannelDTO dto = YoutubeChannelDTO.builder()
                .channelId("iiiiiiiii")
                .title("ttttttttt")
                .description("dddddddddd")
                .publishDate(new Timestamp(System.currentTimeMillis()))
                .viewCount(100L)
                .subscriberCount(10000L)
                .subscriberCountHidden(false)
                .videoCount(1234L)
                .brandingKeywords("bbbbbbbbb ccccccccccc")
                .thumbnailMedium("mmmmmmmmmmmm")
                .thumbnailHigh("hhhhhhhhhhhh")
                .build();
        List<YoutubeChannelDTO> list = new ArrayList<>(1);
        list.add(dto);
        int cnt = m_youtubeRepository.saveChannelList(list);
        assertThat(cnt > 0);
    }

    @Test
    public void testSaveCommentList(){
        YoutubeCommentDTO dto = YoutubeCommentDTO.builder()
                .commentId("dddddddddddd")
                .totalReplyCount(11L)
                .authorDisplayName("aaaaaaaa")
                .textOriginal("ttttttttt")
                .textDisplay("dddddddddd")
                .publishDate(new Timestamp(System.currentTimeMillis()))
                .updateDate(new Timestamp(System.currentTimeMillis()))
                //.replyCount(22)
                .parentId("ppppppp")
                //.channelId(1L)
                .channelId("cccccccc")
                .youtubeId(11L)
                .build();
        List<YoutubeCommentDTO> list = new ArrayList<>();
        list.add(dto);
        int cnt = m_youtubeRepository.saveCommentList(list);
        assertThat(cnt > 0);
    }

    @Test
    @DisplayName("?????????????????? 0?????? insert ???????????? ??????")
    public void testArrayListSizeZeroInsert(){
        List<YoutubeCommentDTO> list = new ArrayList<>(0);
        int cnt = m_youtubeRepository.saveCommentList(list);
        System.out.println("cnt : " + cnt);
    }

    @Test
    @Transactional
    public void testPlayListInsert(){
        Map<String, Object> tmp = m_youtubeClient.getYoutubePlayListDTO("??????????????????", null, null);
        List<YoutubePlayListDTO> playLists = (List<YoutubePlayListDTO>) tmp.get("RESULT_LIST");
        //List<YoutubePlayListDTO> playLists = m_youtubeClient.getYoutubePlayListDTO("??????????????????");

        System.out.println("================== insert ?????? ??? id??? ?????? ?????? ==================");
        playLists.stream().forEach(list ->{
            System.out.println("insert ?????? list.getId() : " + list.getId());
        });
        System.out.println("================== insert ?????? ??? id??? ?????? ??? ==================");

        int cnt = m_youtubeRepository.savePlayList(playLists);

        System.out.println("================== insert ?????? id??? ?????? ?????? ==================");
        playLists.stream().forEach(list ->{
            System.out.println("insert ?????? list.getId() : " + list.getId());
        });
        System.out.println("================== insert ?????? id??? ?????? ??? ==================");

        assertThat(cnt > 0);
        System.out.println("cnt : " + cnt);
    }

    @Test
    public void testGetChannelMap(){
        List<String> param = new ArrayList<>(2);
        param.add("UC1IsspG2U_SYK8tZoRsyvfg");
        param.add("iiiiiiiii");

        List<YoutubeChannelDTO> res = m_youtubeRepository.getChannelList(param);

        assertThat(res != null);

        res.stream().forEach(dto -> {
            System.out.println(dto.toString());
        });
    }
}
