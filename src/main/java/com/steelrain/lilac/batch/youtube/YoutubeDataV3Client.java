package com.steelrain.lilac.batch.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.exception.LilacCommentDisabledVideoException;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class YoutubeDataV3Client implements IYoutubeClient{

    private final APIConfig m_apiConfig;

    public YoutubeDataV3Client(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;
    }


    @Override
    public List<YoutubePlayListDTO> getYoutubePlayListDTO(String keyword) {
        if(!StringUtils.hasText(keyword)){
            return new ArrayList<>(0);
        }

        List<YoutubePlayListDTO> resultList = null;
        try{
            // 개발용 임시 재생목록 json 문자열, 테스트할때 마다 api 호출은 부담스럽다
            String tmp = "{\"items\":[{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL6i7rGeEmTvqEjTJF3PJR4a1N9KTPpfw0\"},\"snippet\":{\"channelId\":\"UCIimf8pEC9AP0N_9FGW_P_w\",\"channelTitle\":\"이기적 영진닷컴\",\"description\":\"이 동영상은 [이기적 정보처리기사 필기 절대족보] 도서 내용을 바탕으로 제작되었습니다. 도서 자세히 보기 ...\",\"publishedAt\":\"2022-03-15T06:55:57.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/JhKOsZuMDWs/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/JhKOsZuMDWs/mqdefault.jpg\"}},\"title\":\"정보처리기사 필기 절대족보(2022년)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLpYNFXUfkvDoKIPr-o9GiD_Jr7tmyTjNu\"},\"snippet\":{\"channelId\":\"UCPb3m8raQQATP-nlPwDRRXA\",\"channelTitle\":\"길벗시나공 IT\",\"description\":\"\",\"publishedAt\":\"2022-09-27T05:44:38.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/bKaHDEkfPdw/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/bKaHDEkfPdw/mqdefault.jpg\"}},\"title\":\"[시나공 정보처리기사] 필기 토막강의\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLf4CcebuSM9C15uQC9CLLnzgh-phsnFUT\"},\"snippet\":{\"channelId\":\"UC74f_na2q-p-YExFb-mojMw\",\"channelTitle\":\"에듀윌 기술자격증\",\"description\":\"\",\"publishedAt\":\"2022-10-04T02:09:43.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/lSPFhgAoSsc/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/lSPFhgAoSsc/mqdefault.jpg\"}},\"title\":\"\\uD83D\\uDCBB정보처리기사 특강 모음 zip｜에듀윌 정보처리기사\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL6i7rGeEmTvoea5d4Xr_Awhi_ES5GvkBA\"},\"snippet\":{\"channelId\":\"UCIimf8pEC9AP0N_9FGW_P_w\",\"channelTitle\":\"이기적 영진닷컴\",\"description\":\"이 동영상은 [이기적 정보처리기사 필기+실기 환상의 콤비] 도서 내용을 바탕으로 제작되었습니다. ❤️ 기적의 합격 강의는 책의 내용 ...\",\"publishedAt\":\"2022-08-18T00:14:50.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/CUg1aAEosmo/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/CUg1aAEosmo/mqdefault.jpg\"}},\"title\":\"정보처리기사 환상의 콤비(2023년)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLwXldj55mFgDnGuNrQ99nrvL3Es9htWn9\"},\"snippet\":{\"channelId\":\"UChC561jgj-iqDpWwgr4cPyA\",\"channelTitle\":\"두목넷 익스터디\",\"description\":\"\",\"publishedAt\":\"2022-03-22T05:28:12.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/mWCZfRnGiq8/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/mWCZfRnGiq8/mqdefault.jpg\"}},\"title\":\"정보처리기사 실기 기출문제(2022)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLpYNFXUfkvDrP24eAgdQxo4bn1VT4zq0A\"},\"snippet\":{\"channelId\":\"UCPb3m8raQQATP-nlPwDRRXA\",\"channelTitle\":\"길벗시나공 IT\",\"description\":\"\",\"publishedAt\":\"2022-05-30T05:03:17.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/Mg4rh28bgKc/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/Mg4rh28bgKc/mqdefault.jpg\"}},\"title\":\"[시나공 정보처리기사 QnE] 토막 강의\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLz95GL3y9Hv3Bp30P4Zx7Le_2gYPurJSe\"},\"snippet\":{\"channelId\":\"UCZD_mSIrG7VC4Im2lOMZMmQ\",\"channelTitle\":\"기사퍼스트 권우석\",\"description\":\"\",\"publishedAt\":\"2022-07-25T06:54:07.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/5Z210firEBk/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/5Z210firEBk/mqdefault.jpg\"}},\"title\":\"[문제풀이] 정보처리기사/산업기사 프로그래밍 언어\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLxpa-X2Ni9I8FPRyWtoMv0_bu48IaqqwB\"},\"snippet\":{\"channelId\":\"UC1odjf0Seqo48PbjVeOUS1w\",\"channelTitle\":\"에듀윌 자격증\",\"description\":\"\",\"publishedAt\":\"2022-12-21T04:34:39.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/3cMX7FPwjBU/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/3cMX7FPwjBU/mqdefault.jpg\"}},\"title\":\"2023 정보처리기사 실기｜Step1 비전공자 눈높이 프로그래밍 언어｜에듀윌 자격증\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLb-PqAw844hl1ejfoI3QckR2S7jiBqq5p\"},\"snippet\":{\"channelId\":\"UCMy1_s4848mqA_K2etd5HPQ\",\"channelTitle\":\"일타클래스 - 기사 자격증\",\"description\":\"\",\"publishedAt\":\"2022-06-07T08:03:57.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/bbTfeX3Z0eI/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/bbTfeX3Z0eI/mqdefault.jpg\"}},\"title\":\"정보처리기사\\uD83D\\uDEA9\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLpYNFXUfkvDra1q71A8DukD1QsbtrbwWU\"},\"snippet\":{\"channelId\":\"UCPb3m8raQQATP-nlPwDRRXA\",\"channelTitle\":\"길벗시나공 IT\",\"description\":\"\",\"publishedAt\":\"2022-03-14T05:35:54.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/kd4HLxQrsww/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/kd4HLxQrsww/mqdefault.jpg\"}},\"title\":\"[시나공 정보처리산업기사] 실기 토막강의\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLz95GL3y9Hv2mF7U8D12lf9swtIN6dBPX\"},\"snippet\":{\"channelId\":\"UCZD_mSIrG7VC4Im2lOMZMmQ\",\"channelTitle\":\"기사퍼스트 권우석\",\"description\":\"\",\"publishedAt\":\"2022-01-26T02:15:09.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/ynIBUi1NjwA/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/ynIBUi1NjwA/mqdefault.jpg\"}},\"title\":\"[기사퍼스트 X 성안당] 2023 정보처리기사\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLS4in2VtSYNqJXvZen_w0ADYmp16jLOA2\"},\"snippet\":{\"channelId\":\"UC77RQpR7AXFEzzYk-3OT_PQ\",\"channelTitle\":\"1타클래스교육그룹 \",\"description\":\"\",\"publishedAt\":\"2022-04-04T07:26:04.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/H52iHb44EX8/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/H52iHb44EX8/mqdefault.jpg\"}},\"title\":\"\\uD83D\\uDCD7 임성애 │ 정보처리기사  │ 샘플강의\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL5EH7R1tqZPSQpf9j9nZlvEzPek0vLckC\"},\"snippet\":{\"channelId\":\"UC_lFKdLqkQpB8RlG4Jb9RcQ\",\"channelTitle\":\"원딴\",\"description\":\"\",\"publishedAt\":\"2022-06-07T07:49:09.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/zWNO2uYgXR0/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/zWNO2uYgXR0/mqdefault.jpg\"}},\"title\":\"정보처리기사\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL-itB98qno4dka-ktOSF7yMy3-lADK5aE\"},\"snippet\":{\"channelId\":\"UCM4VjT6NfhYvzr7dm_TQJzA\",\"channelTitle\":\"공부영상보기\",\"description\":\"청보처리기사 공부용. 출판사에서 만든 재생목록 순서가 몇 개 틀려 편집한 재생목록입니다. 책 내용이 괜찮으니 책 구매하시고 강의 ...\",\"publishedAt\":\"2022-03-20T04:26:42.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/CCX42pYSEeI/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/CCX42pYSEeI/mqdefault.jpg\"}},\"title\":\"정보처리기사 환상의콤비\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLvC178Ep-C0GdsHVUpUe9fzkK7nff8sh9\"},\"snippet\":{\"channelId\":\"UCDoFvsVS3ILK6KH94X9y5Gw\",\"channelTitle\":\"공부망토\",\"description\":\"\",\"publishedAt\":\"2022-03-18T14:46:59.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/aZDy-0dxzkI/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/aZDy-0dxzkI/mqdefault.jpg\"}},\"title\":\"정보처리기사\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLwXldj55mFgCGTEhsA1czBcuQRdVfJ1t7\"},\"snippet\":{\"channelId\":\"UChC561jgj-iqDpWwgr4cPyA\",\"channelTitle\":\"두목넷 익스터디\",\"description\":\"\",\"publishedAt\":\"2022-02-08T07:42:30.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/9Z9ge1jipOE/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/9Z9ge1jipOE/mqdefault.jpg\"}},\"title\":\"정보처리산업기사 필기 이론 강의\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLhBLJ6fymj86umhxuWmLe6KBPWA8_nZ5U\"},\"snippet\":{\"channelId\":\"UCTEJWKtOkRq00If6PYB07nQ\",\"channelTitle\":\"한진섭\",\"description\":\"\",\"publishedAt\":\"2022-01-19T01:13:04.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/9WZ-7z5m4k4/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/9WZ-7z5m4k4/mqdefault.jpg\"}},\"title\":\"정보처리기사 1장\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLECaZOlMHhaTkniRefFK7kJOVMsVL6_i9\"},\"snippet\":{\"channelId\":\"UCGOcb8MFjK0V9XMLd9g9zrw\",\"channelTitle\":\"자격증 단박합격 단합!\",\"description\":\"\",\"publishedAt\":\"2022-08-04T00:28:26.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/gOIOAOprJxU/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/gOIOAOprJxU/mqdefault.jpg\"}},\"title\":\"정보처리기사 1과목 [단합]\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLECaZOlMHhaSyrU53HsgMuVbwY4-hJtyX\"},\"snippet\":{\"channelId\":\"UCGOcb8MFjK0V9XMLd9g9zrw\",\"channelTitle\":\"자격증 단박합격 단합!\",\"description\":\"\",\"publishedAt\":\"2022-08-05T07:11:26.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/9NsuGlFv5s0/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/9NsuGlFv5s0/mqdefault.jpg\"}},\"title\":\"정보처리기사 2과목 [단합]\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLndgW9eOPEN4hMSNm8VbF5EgI-0PjS5gQ\"},\"snippet\":{\"channelId\":\"UCmhWgk6oDkafLwaDIcasUYg\",\"channelTitle\":\"이수호\",\"description\":\"\",\"publishedAt\":\"2022-07-12T11:14:37.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/5t9DqC_Sb50/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/5t9DqC_Sb50/mqdefault.jpg\"}},\"title\":\"(실기)정보처리기사\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL6i7rGeEmTvoBRQgulMnGM0Du0YtjVFhC\"},\"snippet\":{\"channelId\":\"UCIimf8pEC9AP0N_9FGW_P_w\",\"channelTitle\":\"이기적 영진닷컴\",\"description\":\"이 동영상은 [이기적 정보처리산업기사 실기 기본서] 도서 내용을 바탕으로 제작되었습니다. ❤️ 기적의 합격 강의는 책의 내용 전체 ...\",\"publishedAt\":\"2022-09-19T22:55:16.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/qf18uWStcVQ/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/qf18uWStcVQ/mqdefault.jpg\"}},\"title\":\"정보처리산업기사 실기 기본서(2023년)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLECaZOlMHhaQ7FCsii7KBY7Qgy2CKVx6k\"},\"snippet\":{\"channelId\":\"UCGOcb8MFjK0V9XMLd9g9zrw\",\"channelTitle\":\"자격증 단박합격 단합!\",\"description\":\"\",\"publishedAt\":\"2022-08-12T05:23:02.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/Lf27m8vEImU/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/Lf27m8vEImU/mqdefault.jpg\"}},\"title\":\"정보처리기사 4과목 [단합]\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLoQ7DOAeGL-he_GNkBiPcBgKzmYOzqGgb\"},\"snippet\":{\"channelId\":\"UCkZXCS6Vd3jetngmORaNMwA\",\"channelTitle\":\"비스서민(이범수)\",\"description\":\"\",\"publishedAt\":\"2022-10-03T00:37:54.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/4tuPen3R6Lk/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/4tuPen3R6Lk/mqdefault.jpg\"}},\"title\":\"정보처리산업기사 실기\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLjR_tKJ5V47cn9ifwLasmLAQCQmFUlRKS\"},\"snippet\":{\"channelId\":\"UCwswN2jYhJEVYDn1Uvfz4qw\",\"channelTitle\":\"해커린이\",\"description\":\"\",\"publishedAt\":\"2022-07-16T09:20:04.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/DSXQ80e14kA/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/DSXQ80e14kA/mqdefault.jpg\"}},\"title\":\"정보처리기사 실기(이기적)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLniy99c_7Zfr0LB3z9-7AXEF-wNfHCXuo\"},\"snippet\":{\"channelId\":\"UCtGmUJ92gdjC5GwzGYj7Tug\",\"channelTitle\":\"흥달쌤\",\"description\":\"\",\"publishedAt\":\"2021-12-12T12:18:09.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/57MjtQlCBEE/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/57MjtQlCBEE/mqdefault.jpg\"}},\"title\":\"공지\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL6i7rGeEmTvqkVLNSEaNIrXB7oXDbwlgt\"},\"snippet\":{\"channelId\":\"UCIimf8pEC9AP0N_9FGW_P_w\",\"channelTitle\":\"이기적 영진닷컴\",\"description\":\"이 동영상은 [이기적 사무자동화산업기사 필기 절대족보] 도서 내용을 바탕으로 제작되었습니다. 도서 자세히 보기 ...\",\"publishedAt\":\"2022-08-16T08:21:28.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/dMTsdBxyIxo/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/dMTsdBxyIxo/mqdefault.jpg\"}},\"title\":\"사무자동화산업기사 필기 절대족보\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL7Nlq9zvCybyB4fPOtIUOiLfJwbEXZez1\"},\"snippet\":{\"channelId\":\"UC4REETmYtlvqHunx2tl2Pmg\",\"channelTitle\":\"자격증 교육 1위 해커스 | 기사·IT·KBS한국어\",\"description\":\"\",\"publishedAt\":\"2022-11-10T04:29:02.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/rX6CTha67r0/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/rX6CTha67r0/mqdefault.jpg\"}},\"title\":\"자격증 꿀팁! 여기 다 모았다 \\uD83C\\uDF6F 합격 꿀팁 특강\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL4HeKDLlDW4xdk7-TMEyTpxX1FAwL7I-1\"},\"snippet\":{\"channelId\":\"UCax1PuasSwgksdEts9cKESA\",\"channelTitle\":\"경일대학교 스마트보안학과\",\"description\":\"스마트보안 전공 관련 자격증들을 소개해드립니다.\",\"publishedAt\":\"2022-07-09T08:08:12.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/zcVY9ZfWIZs/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/zcVY9ZfWIZs/mqdefault.jpg\"}},\"title\":\"전공관련 자격증 소개\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLetkvXWioaD3Kh5PdlW0eEy2ED5jtcme0\"},\"snippet\":{\"channelId\":\"UCELmTabA2aKALSE5MOgMfEg\",\"channelTitle\":\"프로그래밍 - 푸른서\",\"description\":\"\",\"publishedAt\":\"2022-04-01T13:59:38.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/f1pHS8TDEa4/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/f1pHS8TDEa4/mqdefault.jpg\"}},\"title\":\"C언어 - 개발도구, 기타\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLO8mC_VLlYJJVa1sO5GcVZj5NLkEAtWL7\"},\"snippet\":{\"channelId\":\"UCOy4kvOkBbTE_b9LOXlFaQQ\",\"channelTitle\":\"jiyi just\",\"description\":\"\",\"publishedAt\":\"2022-06-13T03:08:34.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/avqSOREFDrk/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/avqSOREFDrk/mqdefault.jpg\"}},\"title\":\"정처기\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLilm05xZEX0CA4AdkZyRcAl_Jh5JPZzKb\"},\"snippet\":{\"channelId\":\"UCyZSdHhdzbYwAWqcznQlXJA\",\"channelTitle\":\"아이티리치\",\"description\":\"\",\"publishedAt\":\"2022-11-15T08:01:13.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/HiykMpLp5Q8/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/HiykMpLp5Q8/mqdefault.jpg\"}},\"title\":\"SAP ERP 취업 성공 팁\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLr-B885zQqLwwh03PdOerjJNrxuGs5W89\"},\"snippet\":{\"channelId\":\"UC2QXnvo3GtXQopQp5F3fnmA\",\"channelTitle\":\"김티처\",\"description\":\"\",\"publishedAt\":\"2022-01-19T03:03:41.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/QDL9bK6GK6o/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/QDL9bK6GK6o/mqdefault.jpg\"}},\"title\":\"자격검정형(정보처리산업기사실기)\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLv1cSWv9crRyCUZEZNUF0MlNs0poFW7T2\"},\"snippet\":{\"channelId\":\"UCI17tjaQhgD-OjfqhhiL0uw\",\"channelTitle\":\"Polo B\",\"description\":\"\",\"publishedAt\":\"2022-04-06T09:25:29.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/9n41dhyT1sk/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/9n41dhyT1sk/mqdefault.jpg\"}},\"title\":\"플리\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLGguVRsemB-xgakxbOzmVMdF_GKD4wCws\"},\"snippet\":{\"channelId\":\"UCjyV9HNXBc9SzaQSpoowykw\",\"channelTitle\":\"변성욱\",\"description\":\"시작.\",\"publishedAt\":\"2021-12-03T17:31:58.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/3laLavGghc0/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/3laLavGghc0/mqdefault.jpg\"}},\"title\":\"\\\\0.0/\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLUNcucvBMDO7pmozl9-hZQj1l_sB9feIx\"},\"snippet\":{\"channelId\":\"UCwYe356MDULKcGfSJZjR1-Q\",\"channelTitle\":\"솔방울\",\"description\":\"많관부,,\",\"publishedAt\":\"2021-12-19T15:51:23.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/sQfyDKU42CQ/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/sQfyDKU42CQ/mqdefault.jpg\"}},\"title\":\"solbangool vlog\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLBca1L8fP3jjKnmPcVUz2Sdey-3d22ZJC\"},\"snippet\":{\"channelId\":\"UCcr6UiA45PdSt0cnrsyq2OQ\",\"channelTitle\":\"응슷\",\"description\":\"\",\"publishedAt\":\"2022-03-30T20:45:17.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/cvnDAYLpnNg/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/cvnDAYLpnNg/mqdefault.jpg\"}},\"title\":\"컴\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLAH4lhzZpsWfmA6iiTJoLsZkOFv5HjKOz\"},\"snippet\":{\"channelId\":\"UCL6spa1Jw8CD98_qtPF2k_w\",\"channelTitle\":\"장유진\",\"description\":\"\",\"publishedAt\":\"2022-01-31T11:28:05.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/dFSwhDrXab4/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/dFSwhDrXab4/mqdefault.jpg\"}},\"title\":\"1\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLX3ac8MqoLh6UHp_BOHNKDNrXcYqu7bUa\"},\"snippet\":{\"channelId\":\"UCZidJ-rezVb1BjQfE7By4cg\",\"channelTitle\":\"망고\",\"description\":\"\",\"publishedAt\":\"2022-01-05T15:15:33.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/T2UUTwvVoaM/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/T2UUTwvVoaM/mqdefault.jpg\"}},\"title\":\"노래방\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLgHZk4sRc1qJ8GkJFmTBIiZ5HVuwxUOP6\"},\"snippet\":{\"channelId\":\"UCzoL0haxxRyJt0PL3F3wEvw\",\"channelTitle\":\"희소\",\"description\":\"\",\"publishedAt\":\"2022-03-06T15:24:11.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/0WauKZpIzGo/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/0WauKZpIzGo/mqdefault.jpg\"}},\"title\":\"VLOG\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLHiv9JT2cZsaztPkQqDN_hPUuUPPmiWIv\"},\"snippet\":{\"channelId\":\"UCNPLHBc64VYsymzkRVx4GhQ\",\"channelTitle\":\"최코코\",\"description\":\"\",\"publishedAt\":\"2022-02-15T10:55:41.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/ifLQnfpYEGw/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/ifLQnfpYEGw/mqdefault.jpg\"}},\"title\":\"L\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLRJ9Md0GKEW937bXSnYly5ujkMZii6dPc\"},\"snippet\":{\"channelId\":\"UCLkIABW1gFXFVtgAEPf_WVg\",\"channelTitle\":\"김지용\",\"description\":\"\",\"publishedAt\":\"2021-12-26T09:35:30.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/OCKm0XuK0Fs/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/OCKm0XuK0Fs/mqdefault.jpg\"}},\"title\":\"손사모\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLp0Aqv8yqw1HBIYcc_wdPn6bTOt740AEw\"},\"snippet\":{\"channelId\":\"UCbT5-a85uG5oCGZHCKn0ukw\",\"channelTitle\":\"김김김\",\"description\":\"\",\"publishedAt\":\"2022-02-12T04:59:09.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/QdfcrCxQFfU/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/QdfcrCxQFfU/mqdefault.jpg\"}},\"title\":\"피아노\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLzWq1yFHxefz-5J1SRz1oYrBmm48ZatRo\"},\"snippet\":{\"channelId\":\"UC6kq0lSgIVAF43XQ_QA-89w\",\"channelTitle\":\"bear like\",\"description\":\"\",\"publishedAt\":\"2022-02-14T13:25:54.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/I0_ZXHzKysc/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/I0_ZXHzKysc/mqdefault.jpg\"}},\"title\":\"ㅇㅇㅇ\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLspoznJThfCQPJFB-7e6N6ACc0t_iP5mU\"},\"snippet\":{\"channelId\":\"UCixdkdKvCREjjGdC4rrVgbw\",\"channelTitle\":\"이지환\",\"description\":\"\",\"publishedAt\":\"2022-10-23T03:13:53.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/ofdIpXtkAAQ/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/ofdIpXtkAAQ/mqdefault.jpg\"}},\"title\":\"playlist\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLge1E26MvSSjeenQOtcwkI20Su7zQhAv6\"},\"snippet\":{\"channelId\":\"UChR1ep268-Y7h1IpPnTIuDA\",\"channelTitle\":\"Op Su\",\"description\":\"\",\"publishedAt\":\"2022-01-31T16:42:35.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/TvL_C5N4-C0/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/TvL_C5N4-C0/mqdefault.jpg\"}},\"title\":\"가요DANCE\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL7xfYNxUyUVNnaypnOEUFtzeH8OBTrlUj\"},\"snippet\":{\"channelId\":\"UCBXryPJY01jTkWCZ2tdKjkw\",\"channelTitle\":\"수훈 함\",\"description\":\"\",\"publishedAt\":\"2022-04-01T14:41:20.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/xpFp4aasopw/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/xpFp4aasopw/mqdefault.jpg\"}},\"title\":\"지환\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLvFXP3VvVZ5t0suoM1oGUgabaK85NirLy\"},\"snippet\":{\"channelId\":\"UCRdZs5eSxVgCXMHaiCj1zKA\",\"channelTitle\":\"티라노\",\"description\":\"\",\"publishedAt\":\"2022-02-19T15:14:32.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/OJhEUTIrqaQ/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/OJhEUTIrqaQ/mqdefault.jpg\"}},\"title\":\"개발\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLMNs9RWSpTQczzfdAr2tkWe224xpirIfJ\"},\"snippet\":{\"channelId\":\"UCdXkP-ydBWEl-Wn1lyaRpcQ\",\"channelTitle\":\"영앙\",\"description\":\"\",\"publishedAt\":\"2022-05-12T22:45:25.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/hh5GKVa8VtM/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/hh5GKVa8VtM/mqdefault.jpg\"}},\"title\":\"신세대적음악\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PL8bJTQjEiePTIgJjKgx-00Vb67LClmMjP\"},\"snippet\":{\"channelId\":\"UCdRrwUqNbAAQzij14MQrioA\",\"channelTitle\":\"땅콩이눈나\",\"description\":\"유산소 할 때 듣기 좋은 노래.\",\"publishedAt\":\"2022-03-19T11:52:33.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/zspA0I7xme4/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/zspA0I7xme4/mqdefault.jpg\"}},\"title\":\"유산소\"}},{\"id\":{\"kind\":\"youtube#playlist\",\"playlistId\":\"PLx5rme9bWeStcAC3gdBW4Omuw0mEtjGES\"},\"snippet\":{\"channelId\":\"UCvDxxNMRyH6jdSK2LQxVvIA\",\"channelTitle\":\"전주 이젠컴퓨터.IT아카데미학원\",\"description\":\"\",\"publishedAt\":\"2022-02-15T03:18:36.000Z\",\"thumbnails\":{\"high\":{\"url\":\"https://i.ytimg.com/vi/NJ-u6vnyrNU/hqdefault.jpg\"},\"medium\":{\"url\":\"https://i.ytimg.com/vi/NJ-u6vnyrNU/mqdefault.jpg\"}},\"title\":\"인터뷰\"}}],\"nextPageToken\":\"CDIQAA\",\"pageInfo\":{\"resultsPerPage\":50,\"totalResults\":275}}";
            SearchListResponse response = JacksonFactory.getDefaultInstance().fromString(tmp, SearchListResponse.class);

            if(response == null || (response != null && response.getItems().size() == 0)){
                return new ArrayList<>(0);
            }

            resultList = new ArrayList<>(response.getItems().size());
            for (SearchResult sr : response.getItems()) {
                if (sr.isEmpty()) {
                    continue;
                }

                String playListId = sr.getId().getPlaylistId();
                if (!StringUtils.hasText(playListId)) {
                    continue;
                }

                YoutubePlayListDTO dto = YoutubePlayListDTO.convertToYoutubePlayListDTO(sr);
                resultList.add(dto);
            }
        }catch (IOException ioe){
            if(resultList == null){
                resultList = new ArrayList<>(0);
            }
            throw new LilacYoutubeAPIException("유튜브 재생목록 키워드검색 도중 예외 발생", ioe, keyword);
        }
        return resultList;
    }

    /*@Override
    public List<YoutubePlayListDTO> getYoutubePlayListDTO(String keyword) {
        List<YoutubePlayListDTO> resultList = null;
        try {
            YouTube youtube = getYoutubeObject();
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setMaxResults(50L)
                    //.setOrder("date")
                    .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");

            SearchListResponse response = request.execute();
            if(response == null || (response != null && response.getItems().size() == 0)){
                return new ArrayList<>(0);
            }

            resultList = new ArrayList<>(response.getItems().size());
            for (SearchResult sr : response.getItems()){
                if(sr.isEmpty()) {
                    continue;
                }

                String playListId = sr.getId().getPlaylistId();
                if(!StringUtils.hasText(playListId)){
                    continue;
                }

                YoutubePlayListDTO dto = YoutubePlayListDTO.convertToYoutubePlayListDTO(sr);
                resultList.add(dto);
            }

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록 키워드검색 도중 예외 발생", e, keyword);
        }
        return resultList;
    }*/

    /*
        TODO : 재생목록에 등록된 영상들 중 간혹 중복되어 들어 오는 영상들이 있다. 중복을 제거해주는 작업이 필요하다 
     */
    @Override
    public List<YoutubeVideoDTO> getVideoDTOListByPlayListId(String playListId){
        if(!StringUtils.hasText(playListId)){
            return new ArrayList<>(0);
        }

        List<YoutubeVideoDTO> resultList = null;
        try {
            YouTube youtubeObj = getYoutubeObject();
            YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
            PlaylistItemListResponse response = request.setMaxResults(5L)
                    .setPlaylistId(playListId)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .execute();
            resultList = new ArrayList<>(response.getItems().size());

            for(PlaylistItem item : response.getItems()){
                if(isNullablePlaylistItem(item)){
                    continue;
                }
                VideoListResponse videoListResponse = getVideoDetail(item.getContentDetails().getVideoId());
                if(isNullableVideoListResponse(videoListResponse)){
                    continue;
                }

                if(YoutubeVideoDTO.convertYoutubeVideoDTO(item, videoListResponse.getItems().get(0)) == null){
                    continue;
                }
                resultList.add(YoutubeVideoDTO.convertYoutubeVideoDTO(item, videoListResponse.getItems().get(0)));
            }
        } catch (IOException | GeneralSecurityException e) {
            if(resultList == null){
                resultList = new ArrayList<>(0);
            }
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
        }
        return resultList;
    }

    // 유튜브API를 통해 유튜브 영상의 댓글리스트를 가져온다.
    @Override
    public List<YoutubeCommentDTO> getCommentList(String videoId) {
        if(!StringUtils.hasText(videoId)){
            return new ArrayList<>(0);
        }

        List<YoutubeCommentDTO> commentList = null;
        try {
            YouTube youTube = getYoutubeObject();
            YouTube.CommentThreads.List request = youTube.commentThreads()
                    .list("snippet,replies");
            CommentThreadListResponse apiResponse = request.setKey(m_apiConfig.getYoutubeKey())
                    .setMaxResults(10L)
                    .setTextFormat("plainText")
                    .setVideoId(videoId)
                    .execute();

            if(apiResponse.getItems() == null && apiResponse.getItems().size() == 0){
                return new ArrayList<>(0);
            }

            List<CommentThread> threadsList = apiResponse.getItems();
            commentList = new ArrayList<>(threadsList.size());

            for(CommentThread thread : threadsList){
                if(thread.getSnippet() == null && thread.getSnippet().getTopLevelComment() == null &&
                        thread.getSnippet().getTopLevelComment().getSnippet() == null){
                    continue;
                }
                YoutubeCommentDTO dto = YoutubeCommentDTO.builder()
                        .commentId(thread.getId())
                        .totalReplyCount(thread.getSnippet().getTotalReplyCount())
                        .authorDisplayName(thread.getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName())
                        .textOriginal(thread.getSnippet().getTopLevelComment().getSnippet().getTextOriginal())
                        .textDisplay(thread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay())
                        .publishDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getPublishedAt().getValue()))
                        .updateDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getUpdatedAt().getValue()))
                        //.parentId() TODO : 대댓글 처리는 나중에 한다. -> replies.comments.snippet 에 있는 parentId를 가져와야 한다.
                        //.channelId() DB에 저장하기 직전에 채널정보를 DB에 insert 하고 얻은 id값으로 직접 세팅해주므로 현재는 할 필요가 없다
                        //.youtubeId() DB에 저장하기 직전에 유튜브영상을 DB에 insert 하고 얻은 id값으로 직접 세팅해주므로 현재는 할 필요가 없다
                        .build();
                commentList.add(dto);
            }
        }catch(GoogleJsonResponseException ge){
            /*
                - 예외가 발생할 경우 null을 반환하지 않도록한다.
                - 길이가 0인 리스트를 반환하므로써 루프에 진입했을때 NPE 가 발생하지 않도록 한다.
             */
            if(commentList == null){
                commentList = new ArrayList<>(0);
            }
            throw new LilacCommentDisabledVideoException("영상의 댓글리스트 가져오기 예외 - 댓글금지영상" + " : videoId - " + videoId, ge, videoId);
        } catch (IOException | GeneralSecurityException e) {
            if(commentList == null){
                commentList = new ArrayList<>(0);
            }
            throw new LilacYoutubeAPIException("영상의 댓글리스트 가져오기 예외" + " : videoId - " + videoId, e, videoId);
        }
        return commentList;
    }

    @Override
    public YoutubeChannelDTO getChannelInfo(String channelId) {
        // 테스트 채널 id :  UCZD_mSIrG7VC4Im2lOMZMmQ
        YoutubeChannelDTO resultDTO = null;
        try {
            YouTube youTubeObj = getYoutubeObject();
            YouTube.Channels.List request = youTubeObj.channels()
                    .list("brandingSettings,contentDetails,id,snippet,statistics,status,topicDetails");
            ChannelListResponse response = request.setKey(m_apiConfig.getYoutubeKey()).setId(channelId).execute();
            /*
            private Long id;
            private String channelId;
            private String title;
            private String desc;
            private Timestamp publishDate;
            private Long viewCount;
            private Long subscriberCount;
            private Boolean subscriberCountHidden;
            private Long videoCount;
            private String brandingKeywords;
            private String thumbnailMedium;
            private String thumbnailHigh;
             */
            resultDTO = YoutubeChannelDTO.builder().channelId(response.getItems().get(0).getId())
                    .title(response.getItems().get(0).getSnippet().getTitle())
                    .description(response.getItems().get(0).getSnippet().getDescription())
                    .publishDate(new Timestamp(response.getItems().get(0).getSnippet().getPublishedAt().getValue()))
                    .viewCount(response.getItems().get(0).getStatistics().getViewCount().longValue())
                    .subscriberCount(response.getItems().get(0).getStatistics().getSubscriberCount().longValue())
                    .subscriberCountHidden(response.getItems().get(0).getStatistics().getHiddenSubscriberCount())
                    .videoCount(response.getItems().get(0).getStatistics().getVideoCount().longValue())
                    .brandingKeywords(response.getItems().get(0).getBrandingSettings().getChannel().getKeywords())
                    .thumbnailMedium(response.getItems().get(0).getSnippet().getThumbnails().getMedium().getUrl())
                    .thumbnailHigh(response.getItems().get(0).getSnippet().getThumbnails().getHigh().getUrl())
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("채널 정보 가져오기 예외", e, channelId);
        }
        return resultDTO;
    }

    // 재생목록에 있는 영상정보의 유효성 체크, null 이면 true 아니면 false
    private boolean isNullablePlaylistItem(PlaylistItem item){
        return item.getSnippet() == null || item.getContentDetails() == null;
    }

    // 영상의 상제정보의 유효성 체크, null 이면 true 아니면 false
    private boolean isNullableVideoListResponse(VideoListResponse videoInfo){
        if(videoInfo.getItems() != null && videoInfo.getItems().size() == 0) {
            return true;
        }

//        if(videoInfo.getStatistics().getCommentCount() == null){
//            return null;
//        }

        Video video = videoInfo.getItems().get(0); // 영상의 상세정보는 List 형식이지만 실제로는 1개만 반환하기 때문에 0번째 요소만 가져와도 된다.

        // 댓글금지 영상을 체크하는 부분
        if(video.getStatistics() == null){
            return true;
        }else{
            if(video.getStatistics().getCommentCount() == null)
                return true;
        }
        return video.getSnippet() == null || video.getContentDetails()  == null;
    }

    // 유튜브API의 클라이언트 객체를 반환한다
    private YouTube getYoutubeObject() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("lilac").build();
    }

    // 유튜브 영상의 상세정보를 반환한다
    private VideoListResponse getVideoDetail(String videoId){
        // 테스트용 영상id : JhKOsZuMDWs
        try {
            YouTube youTubeObj = getYoutubeObject();
            YouTube.Videos.List request = youTubeObj.videos()
                    .list("snippet,contentDetails,statistics");
            return request.setId(videoId).setKey(m_apiConfig.getYoutubeKey()).execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, videoId);
        }
    }

     /* public DateTime getDateTimeTest(){
        return DateTime.parseRfc3339(String.format("%d-01-01T00:00:00Z",LocalDateTime.now().getYear()));
    }*/

    /*getYoutubePlayListDTO 메서드로 대체하여 필요없으므로 주석처리
    @Override
    public SearchListResponse getYoutubePlayList(String keyword) {
        SearchListResponse response = null;
        try {
            YouTube youtube = getYoutubeObject();
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
            .setKey(m_apiConfig.getYoutubeKey())
            .setType("playlist")
            .setMaxResults(50L)
            //.setOrder("date")
            .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
            .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");

            response = request.execute();

        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록 키워드검색 도중 예외 발생", e, keyword);
        }
        return response;
    }*/

    /*getVideoDTOListByPlayListId 메서드로 대체 하므로 주석처리
    public PlaylistItemListResponse getVideoListByPlayListId(String playListId){
        PlaylistItemListResponse result = null;
        try {
            YouTube youtubeObj = getYoutubeObject();
            YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
            result = request.setMaxResults(5L)
                        .setPlaylistId(playListId)
                        .setKey(m_apiConfig.getYoutubeKey())
                        .execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
        }
        return result;
    }*/
}
