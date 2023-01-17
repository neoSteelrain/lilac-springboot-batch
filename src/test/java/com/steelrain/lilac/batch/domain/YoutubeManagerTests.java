package com.steelrain.lilac.batch.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class YoutubeManagerTests {

    @Autowired
    private YoutubeManager m_dispatcher;

    @Test
    public void testDispatchYoutube(){
        m_dispatcher.dispatchYoutube();
    }
}
