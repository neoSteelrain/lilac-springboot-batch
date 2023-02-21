package com.steelrain.lilac.batch.domain;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 유튜브 댓글모음의 총 바이트수를 관리하는 클래스
 */
@Component
public class CommentByteCounter {

    private final static int BUFFER_SIZE = 1000;
    // 스트링빌더에 저장된 댓글들의 바이트수를 저장하는 변수
    private int m_totalByteCount;
    
    // 댓글을 저장하는 버퍼
    private static StringBuilder m_sb = new StringBuilder(BUFFER_SIZE);


    // 문자열을 내부 버퍼에 합친다.
    public int addComment(String comment){
        int commentBytes = comment.getBytes(StandardCharsets.UTF_8).length;
        if( (BUFFER_SIZE - m_totalByteCount) > commentBytes ){
            m_totalByteCount += commentBytes;
            m_sb.append(comment);
        }
        return m_totalByteCount;
    }

    // 문자열들이 추가된 내부 버퍼에 저장된 전체문자열의 길이를 반환한다.
    public int getTotalCommentLength(){
        return m_sb.length();
    }

    // 문자열들이 추가된 내부버퍼의 전체문자열을 반환하고 내부버퍼를 초기화한다.
    public String getTotalComment(){
        String result = m_sb.toString();
        m_sb.setLength(0);
        return result;
    }

    // 댓글을 추가할 수 있는지 검사한다.
    public boolean isAddable(String comment){
        int commentBytes = comment.getBytes(StandardCharsets.UTF_8).length;
        return (BUFFER_SIZE - m_totalByteCount) > commentBytes;
    }

    // 내부버퍼에 추가가능한 바이트수를 리턴
    public int getRemainder(){
        return BUFFER_SIZE - m_totalByteCount;
    }
}
