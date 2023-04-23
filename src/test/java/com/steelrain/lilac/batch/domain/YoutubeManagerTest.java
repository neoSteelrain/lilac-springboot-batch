package com.steelrain.lilac.batch.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class YoutubeManagerTest {

    @Autowired
    private YoutubeManager m_youtubeManager;

    /*@Test
    @Transactional
    void fetchYoutubeData() {
        m_youtubeManager.test();
    }*/
}