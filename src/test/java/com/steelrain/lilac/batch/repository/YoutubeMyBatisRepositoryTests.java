package com.steelrain.lilac.batch.repository;


import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
        list.add(dto);

        int cnt = m_youtubeRepository.savePlayList(list);

        assertThat(cnt > 0);
    }

    @Test
    @Transactional
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
        int cnt = m_youtubeRepository.saveChannelInfo(dto);
        assertThat(cnt > 0);
    }

    @Test
    @Transactional
    public void testPlayListInsert(){
        List<YoutubePlayListDTO> playLists = m_youtubeClient.getYoutubePlayListDTO("정보처리기사");

        System.out.println("================== insert 하기 전 id값 출력 시작 ==================");
        playLists.stream().forEach(list ->{
            System.out.println("insert 이전 list.getId() : " + list.getId());
        });
        System.out.println("================== insert 하기 전 id값 출력 끝 ==================");

        int cnt = m_youtubeRepository.savePlayList(playLists);

        System.out.println("================== insert 이후 id값 출력 시작 ==================");
        playLists.stream().forEach(list ->{
            System.out.println("insert 이후 list.getId() : " + list.getId());
        });
        System.out.println("================== insert 이후 id값 출력 끝 ==================");

        assertThat(cnt > 0);
        System.out.println("cnt : " + cnt);
    }
}
