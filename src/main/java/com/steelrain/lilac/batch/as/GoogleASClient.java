package com.steelrain.lilac.batch.as;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.protobuf.ByteString;
import com.steelrain.lilac.batch.datamodel.SentimentDTO;
import com.steelrain.lilac.batch.exception.LilacGoogleASException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 구글 감정분석 API 클라이언트
 * - 컨텐츠한도 : 한번에 분석할 데이터량이 1000 바이트를 넘으면 안된다
 */
@Component
public class GoogleASClient implements ISentimentClient {

    @Override
    public SentimentDTO analyzeComment(String str) {
        if(StringUtils.hasText(str) && (str.getBytes(StandardCharsets.UTF_8).length >= 1000 )){
            return null;
        }

        SentimentDTO result = null;
        try(LanguageServiceClient client = LanguageServiceClient.create()){
            Document doc = Document.newBuilder().setContent(str).setType(Document.Type.PLAIN_TEXT).setLanguage("ko").build();
            Sentiment sentiment = client.analyzeSentiment(doc).getDocumentSentiment();
            result = new SentimentDTO(sentiment.getScore(), sentiment.getMagnitude());

        }catch(IOException ioe){
            throw new LilacGoogleASException("구글 감정분석 API 호출도중 예외", ioe, str);
        }catch(Exception ex){
            throw new LilacGoogleASException("구글 감정분석 API 호출도중 예외", ex, str);
        }
        return result;
    }
}
