package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.config.APIConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.*;

//@Import({APIConfig.class, YoutubeManager.class})

@SpringBootTest
public class DomainClassTests {

    @Autowired
    private APIConfig m_apiApiConfig;

    @Autowired
    private YoutubeManager m_youtubeManager;

    @Test
    public void testYoutubeManager(){
        m_youtubeManager.dispatchYoutube();
    }

    @Test
    public void testCommentCounter(){
        CommentByteCounter cc = new CommentByteCounter();
        cc.addComment("인천일보 아카데미 자바 스프링");

        System.out.println("cc.getTotalCommentLength()1 : " + cc.getTotalCommentLength());
        String tmp = cc.getTotalComment();
        System.out.println("cc.getTotalCommentLength()2 : " + cc.getTotalCommentLength());

        assertThat(StringUtils.hasText(tmp));

        System.out.println("tmp : " + tmp);
    }

    @Test
    @Transactional
    public void testDispatchYoutube(){
        m_youtubeManager.dispatchYoutube();
    }
}
