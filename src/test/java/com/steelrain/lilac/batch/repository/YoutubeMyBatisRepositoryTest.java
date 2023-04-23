package com.steelrain.lilac.batch.repository;

import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class YoutubeMyBatisRepositoryTest {

    @Autowired
    private IYoutubeRepository m_youtubeRepository;

    @Test
    @Transactional
    @DisplayName("재생목록중복저장 테스트")
    public void testSaveDuplicatePl(){
        List<YoutubePlayListDTO> list1 = new ArrayList<>();
        list1.add(createTestPL());
        m_youtubeRepository.savePlayList(list1);
        log.debug("list1 : {}", list1.get(0));

        List<YoutubePlayListDTO> list2 = new ArrayList<>();
        list2.add(createTestPL());
        m_youtubeRepository.savePlayList(list2);
        log.debug("list2 : {}", list2.get(0));

        assertThat(Objects.isNull(list2.get(0).getId())).isTrue();
    }

    private YoutubePlayListDTO createTestPL(){
        YoutubePlayListDTO dto = new YoutubePlayListDTO();
        dto.setPlayListId("aaaaaaaaaa");
        dto.setTitle("ttttttttt");
        dto.setPublishDate(new Timestamp(System.currentTimeMillis()));
        dto.setThumbnailMedium("mmmmmmmmmmmm");
        dto.setThumbnailHigh("hhhhhhhhhhhhhhh");
        dto.setItemCount(100);
        dto.setChannelId(2L);
        return dto;
    }
}