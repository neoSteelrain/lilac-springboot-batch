package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
        m_youtubeManager.doYoutubeBatch();
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
    @DisplayName("댓글모곡 NPE 발생 테스트")
    public void testCommentListNPE(){
        /*
        for(YoutubeCommentDTO dto : video.getComments()) { //  TODO : NPE
            dto.setYoutubeId(video.getId());
            dto.setChannelId(channelDTO.getId());
        }
         */
        // 길이가 0인 리스트에 접근했을때 NPE 가 발생하는지 테스트한다.
        List<YoutubeCommentDTO> comments = new ArrayList<>(0);
        for(YoutubeCommentDTO dto : comments){
            System.out.println("루프 인");
        }
        System.out.println("루프 아웃");
    }

    @Test
    //@Transactional
    public void testDispatchYoutube(){
        m_youtubeManager.doYoutubeBatch();
    }
}
