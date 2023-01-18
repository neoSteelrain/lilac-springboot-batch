package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

//@Import({APIConfig.class, YoutubeManager.class})
@SpringBootTest
public class YoutubeManagerTests {

    @Autowired
    private APIConfig m_apiApiConfig;

    @Test
    public void testDispatchYoutube(){

    }
}
