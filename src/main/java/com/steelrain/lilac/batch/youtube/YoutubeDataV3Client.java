package com.steelrain.lilac.batch.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.steelrain.lilac.batch.config.APIConfig;
import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeCommentDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.datamodel.YoutubeVideoDTO;
import com.steelrain.lilac.batch.exception.LilacYoutubeAPIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Component
public class YoutubeDataV3Client implements IYoutubeClient{

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final APIConfig m_apiConfig;

    public YoutubeDataV3Client(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;
    }

    public SearchListResponse getSearchListResponse(String keyword){
        try {
            YouTube youtube = getYoutubeObject();
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setMaxResults(10L)
                    //.setPageToken(StringUtils.hasText(paramToken) ? null : paramToken)
                    //.setOrder("date")
                    .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/thumbnails/default/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");
            return request.execute();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 키워드로 유튜브 재생목록을 검색하여 재생목록, 페이지토큰을 Map으로 반환한다
     * @param keyword 검색 키워드
     * @param paramToken 이전 검색결과에서 얻은 페이지토큰
     * @return 재생목록, 페이지 토큰 Map, YoutubePlayListMapKey 클래스의 상수값을 키로 사용한다
     */
    @Override
    public Map<String, Object> getYoutubePlayListDTO(String keyword, String paramToken) {
        // 페지지토큰은 따로 검사를 하지 않는다. null 이면 가져올 페이지가 없는것이고 null 이 아니면 페이지가 있는것
        if(!StringUtils.hasText(keyword)){
            Map<String, Object> nullKeywordResult = new HashMap<>(2);
            nullKeywordResult.put(YoutubePlayListMapKey.PLAY_LIST, new ArrayList(0));
            nullKeywordResult.put(YoutubePlayListMapKey.PAGE_TOKEN, null);
            return nullKeywordResult;
        }
        List<YoutubePlayListDTO> playList = null; // 결과 map 에 담을 재생목록
        SearchListResponse apiResponse = null; // 실제 유튜브 API 응답객체
        try{
            // 개발용 임시 재생목록 json 문자열
            String tmp = getTmpLicenseString();
//            String tmp = getTestSubjectListStr();
            apiResponse = JacksonFactory.getDefaultInstance().fromString(tmp, SearchListResponse.class); // 테스트용 재생목록 객체를 얻기 위한 역직렬화

            /*YouTube youtube = getYoutubeObject(); // 실제 코드
            YouTube.Search.List request = youtube.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setMaxResults(2L)
                    .setPageToken(StringUtils.hasText(paramToken) ? null : paramToken)
                    //.setOrder("date")
                    .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/thumbnails/default/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");
            apiResponse = request.execute();*/

            if(apiResponse == null || (apiResponse != null && apiResponse.getItems().size() == 0)){
                Map<String, Object> nullResponseResult = new HashMap<>(2);
                nullResponseResult.put(YoutubePlayListMapKey.PLAY_LIST, new ArrayList(0));
                nullResponseResult.put(YoutubePlayListMapKey.PAGE_TOKEN, null);
                return nullResponseResult;
            }
            playList = new ArrayList<>(apiResponse.getItems().size());
            for (SearchResult sr : apiResponse.getItems()) {
                if (sr.isEmpty()) {
                    continue;
                }
                String playListId = sr.getId().getPlaylistId();
                if (!StringUtils.hasText(playListId)) {
                    continue;
                }
                YoutubePlayListDTO dto = YoutubePlayListDTO.convertToYoutubePlayListDTO(sr);
                playList.add(dto);
            }
        }catch (IOException ioe){
            throw new LilacYoutubeAPIException(String.format("유튜브 재생목록 키워드검색 도중 예외 발생 - 검색 키워드 : %s , 페이지 토큰 : %s", keyword, paramToken),
                                                             ioe, keyword);
        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(YoutubePlayListMapKey.PLAY_LIST, playList);
        resultMap.put(YoutubePlayListMapKey.PAGE_TOKEN, apiResponse.getNextPageToken());
        return resultMap;
    }

    /*
        영상의 상세정보때문에 Videos API 호출할때 java.net.SocketTimeoutException: connect timed out 발생
        상세정보를 빼고 해본다.
     */
    @Override
    public List<YoutubeVideoDTO> getVideoDTOListByPlayListId(String playListId){
        if(!StringUtils.hasText(playListId)){
            return new ArrayList<>(0);
        }
        // 재생목록에 중복된 영상이 들어갈 수 있므로 Map에 저장했다가 List로 리턴한다
        Map<String, YoutubeVideoDTO> videoMap = null;
        String pageToken = null;
        boolean isExit = true;
        int cnt = 0;
        int pageCnt = 0;
        try {
            do{
                YouTube youtubeObj = getYoutubeObject();
                YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
                PlaylistItemListResponse response = request.setMaxResults(50L)
                        .setPlaylistId(playListId)
                        .setKey(m_apiConfig.getYoutubeKey())
                        .setPageToken(StringUtils.hasText(pageToken) ? pageToken : null)
                        .execute();

                if(pageCnt == 0){ // 처음 한번 호출할때만 초기화 작업을 한다
                    int totalResults = response.getPageInfo().getTotalResults().intValue();
                    pageCnt = totalResults / 50;
                    pageCnt = pageCnt + ((totalResults % 50) > 0  ? 1 : 0);
                    videoMap = new HashMap<>(totalResults);
                }
                for(PlaylistItem item : response.getItems()){
                    if(isNullablePlaylistItem(item)){
                        continue;
                    }
                    // 재생목록에 있는 영상정보는 부족하므로 videoId 로 영상의 자세한정보를 API 호출로 가져온다
                    String videoId = item.getSnippet().getResourceId().getVideoId();
//                    VideoListResponse videoListResponse = getVideoDetail(videoId);
//                    if(isNullableVideoListResponse(videoListResponse)){
//                        continue;
//                    }
                    if(videoMap.containsKey(videoId)){
                        continue;
                    }
                    log.debug(String.format("\n========= 재생목록의 영상정보를 가져와서 초기화 시작 - 재생목록id : %s , 영상id : %s", playListId, videoId));
                    log.debug(String.format("\n========= 영상정보 - id : %s , toString : %s", videoId, item.toPrettyString()));
                    videoMap.put(videoId, YoutubeVideoDTO.convertYoutubeVideoDTOnoDetail(item));
                }
                if(cnt < pageCnt){ // 페이징을 해야할지 안할지 체크해서 페이지가 남아있으면 페이징토큰을 얻어오고 아니면 1번만 돌고 종료
                    pageToken = response.getNextPageToken();
                }else{
                    isExit = false;
                }
                ++cnt;
            } while(isExit);
        } catch (IOException | GeneralSecurityException e) {
            log.error(String.format("유튜브 재생목록의 영상조회 도중 예외 발생 - playListId : %s", playListId), e);
            //throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
            if(videoMap.size() == 0){
                return new ArrayList<>(0);
            }
        }
        log.debug(String.format("\n===== 재생목록 : %s 의 영상갯수 : %d : ", playListId, videoMap.values().size()));
        return new ArrayList<>(videoMap.values());
    }

    /*
        재생목록에 있는 모든영상을 가져온다
        중복된 영상이 재생목록에 있을 수 있으므로 걸러내야 한다
     */
//    @Override
//    public List<YoutubeVideoDTO> getVideoDTOListByPlayListId(String playListId){
//        if(!StringUtils.hasText(playListId)){
//            return new ArrayList<>(0);
//        }
//        // 재생목록에 중복된 영상이 들어갈 수 있므로 Map에 저장했다가 List로 리턴한다
//        Map<String, YoutubeVideoDTO> videoMap = null;
//        String pageToken = null;
//        boolean isExit = true;
//        int cnt = 0;
//        int pageCnt = 0;
//        try {
//            do{
//                YouTube youtubeObj = getYoutubeObject();
//                YouTube.PlaylistItems.List request = youtubeObj.playlistItems().list("id,snippet,contentDetails,status");
//                PlaylistItemListResponse response = request.setMaxResults(50L)
//                        .setPlaylistId(playListId)
//                        .setKey(m_apiConfig.getYoutubeKey())
//                        .setPageToken(StringUtils.hasText(pageToken) ? pageToken : null)
//                        .execute();
//
//                if(pageCnt == 0){ // 처음 한번 호출할때만 초기화 작업을 한다
//                    int totalResults = response.getPageInfo().getTotalResults().intValue();
//                    pageCnt = totalResults / 50;
//                    pageCnt = pageCnt + ((totalResults % 50) > 0  ? 1 : 0);
//                    videoMap = new HashMap<>(totalResults);
//                }
//                for(PlaylistItem item : response.getItems()){
//                    if(isNullablePlaylistItem(item)){
//                        continue;
//                    }
//                    // 재생목록에 있는 영상정보는 부족하므로 videoId 로 영상의 자세한정보를 API 호출로 가져온다
//                    String videoId = item.getContentDetails().getVideoId();
//                    VideoListResponse videoListResponse = getVideoDetail(videoId);
//                    if(isNullableVideoListResponse(videoListResponse)){
//                        continue;
//                    }
//                    if(videoMap.containsKey(videoId)){
//                        continue;
//                    }
//                    videoMap.put(videoId, YoutubeVideoDTO.convertYoutubeVideoDTO(item, videoListResponse.getItems().get(0)));
//                }
//                if(cnt < pageCnt){ // 페이징을 해야할지 안할지 체크해서 페이지가 남아있으면 페이징토큰을 얻어오고 아니면 1번만 돌고 종료
//                    pageToken = response.getNextPageToken();
//                }else{
//                    isExit = false;
//                }
//                ++cnt;
//            } while(isExit);
//        } catch (IOException | GeneralSecurityException e) {
//            log.error(String.format("유튜브 재생목록의 영상조회 도중 예외 발생 - playListId : %s", playListId), e);
//            //throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
//            if(videoMap.size() == 0){
//                return new ArrayList<>(0);
//            }
//        }
//        log.debug(String.format("\n===== 재생목록 : %s 의 영상갯수 : %d : ", playListId, videoMap.values().size()));
//        return new ArrayList<>(videoMap.values());
//    }

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
                    .setMaxResults(m_apiConfig.getCommentCount())
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
                /*
                private Long youtubeId;
                private String commentId;
                private Long totalReplyCount;
                private String authorDisplayName;
                private String textOriginal;
                private String textDisplay;
                private Timestamp publishDate;
                private Timestamp updateDate;
                //private Integer replyCount;  // TODO : 필요없는 필드
                private String parentId;
                private String channelId;
                 */
                YoutubeCommentDTO dto = YoutubeCommentDTO.builder()
                        //.youtubeId() DB에 저장하기 직전에 유튜브영상을 DB에 insert 하고 얻은 id값으로 직접 세팅해주므로 현재는 할 필요가 없다
                        .commentId(thread.getId())
                        .totalReplyCount(thread.getSnippet().getTotalReplyCount())
                        .authorDisplayName(thread.getSnippet().getTopLevelComment().getSnippet().getAuthorDisplayName().trim())
                        .textOriginal(thread.getSnippet().getTopLevelComment().getSnippet().getTextOriginal().trim())
                        .textDisplay(thread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay().trim())
                        .publishDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getPublishedAt().getValue()))
                        .updateDate(new Timestamp(thread.getSnippet().getTopLevelComment().getSnippet().getUpdatedAt().getValue()))
                        //.parentId(thread.getSnippet().getTopLevelComment().getSnippet().getParentId().trim()) // TODO : 대댓글 처리는 나중에 한다. -> replies.comments.snippet 에 있는 parentId를 가져와야 한다.
                        .channelId(thread.getSnippet().getChannelId())
                        .build();
                commentList.add(dto);
            }
        } catch(GoogleJsonResponseException ge){
            /*
                - 예외가 발생할 경우 null을 반환하지 않도록한다.
                - 길이가 0인 리스트를 반환하므로써 루프에 진입했을때 NPE 가 발생하지 않도록 한다.
             */
            if(commentList == null){
                commentList = new ArrayList<>(0);
            }
            if(ge.getStatusCode() == 403){
                log.error(String.format("댓글금지 영상 videoId : %s", videoId));
            }else{
                log.error(String.format("영상의 댓글리스트 가져오기 예외 - videoId : %s , Youtube 댓글에러코드 : %d", videoId, ge.getStatusCode()), ge);
            }
        } catch (IOException | GeneralSecurityException e) {
            if(commentList == null){
                commentList = new ArrayList<>(0);
            }
            log.error(String.format("영상의 댓글리스트 가져오기 예외 - videoId : %s , 에러정보 : %s", videoId, e.getMessage()), e);
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
            private String description;
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
        return item.getSnippet() == null || item.getContentDetails() == null || !"public".equals(item.getStatus().getPrivacyStatus());
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
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpRequestInitializer() {
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
            .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/thumbnails/default/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");

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

    private String getTestSubjectListStr(){
        return "{\n" +
                "  \"kind\": \"youtube#searchListResponse\",\n" +
                "  \"etag\": \"HglEHRfe7gkqwYoYz-W3BDBQiUQ\",\n" +
                "  \"nextPageToken\": \"CBkQAA\",\n" +
                "  \"regionCode\": \"KR\",\n" +
                "  \"pageInfo\": {\n" +
                "    \"totalResults\": 11819,\n" +
                "    \"resultsPerPage\": 25\n" +
                "  },\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"8ttC71k2uFaiydiNhsfBgRNxRj8\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLW2UjW795-f6xWA2_MUhEVgPauhGl3xIp\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-12-27T13:41:20Z\",\n" +
                "        \"channelId\": \"UC1IsspG2U_SYK8tZoRsyvfg\",\n" +
                "        \"title\": \"자바의 정석 기초편(2020최신)\",\n" +
                "        \"description\": \"최고의 자바강좌를 무료로 들을 수 있습니다. 어떤 유료강좌보다도 낫습니다.\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oJlCC1DutbA/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oJlCC1DutbA/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oJlCC1DutbA/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"남궁성의 정석코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-12-27T13:41:20Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"gyIYpjB3HgmrIu2AI5MFy2_nXEU\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLuHgQVnccGMCeAy-2-llhw3nWoQKUvQck\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2014-02-22T13:57:29Z\",\n" +
                "        \"channelId\": \"UCvc8kv-i5fvFTJBFAk6n1SA\",\n" +
                "        \"title\": \"Java 입문 수업 (생활코딩)\",\n" +
                "        \"description\": \"자바 입문을 돕기 위한 수업입니다. 텍스트 수업과 소스 코드 그리고 체계적으로 정리된 공식 홈페이지는 아래의 URL로 접근 할 수 ...\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jdTsJzXmgU0/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jdTsJzXmgU0/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jdTsJzXmgU0/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"생활코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2014-02-22T13:57:29Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"rlyVmZGqRnC1CDs5qvMLTXNbzYg\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLVsNizTWUw7HZTPU3GpS7nmshXjKKvlbk\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-06-12T14:33:57Z\",\n" +
                "        \"channelId\": \"UC31Gc42xzclOOi5Gp1xIpZw\",\n" +
                "        \"title\": \"혼자 공부하는 자바\",\n" +
                "        \"description\": \"[혼자 공부하는 자바]는 [이것이 자바다] 콘텐츠를 기반으로 기획된 도서입니다. 동영상 강의 50만 조회수를 기록하는 동안 독자님들로 ...\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/MhLcEITuMZo/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/MhLcEITuMZo/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/MhLcEITuMZo/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"한빛미디어\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-06-12T14:33:57Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"tNVZRUhP3OZr-TvJSmN0QmZFPm8\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLW2UjW795-f5JPTsYHGAawAck9cQRw5TD\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-12-17T23:40:03Z\",\n" +
                "        \"channelId\": \"UC1IsspG2U_SYK8tZoRsyvfg\",\n" +
                "        \"title\": \"자바의 정석 기초편 - 객체지향개념만\",\n" +
                "        \"description\": \"자바의 정석 - 기초편에서 객체지향개념만 모아놓았습니다.\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/CXuA31XcBZ0/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/CXuA31XcBZ0/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/CXuA31XcBZ0/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"남궁성의 정석코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-12-17T23:40:03Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"6gwoyE5-jdMGHs6rUiUqC0JEwls\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLVsNizTWUw7EmX1Y-7tB2EmsK6nu6Q10q\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2022-08-29T06:37:58Z\",\n" +
                "        \"channelId\": \"UC31Gc42xzclOOi5Gp1xIpZw\",\n" +
                "        \"title\": \"[자바 기초 강의] 이것이 자바다(개정판)\",\n" +
                "        \"description\": \"신용권 저자님과 함께하는 『이것이 자바다(개정판)』 강의 소개 영상입니다. 입문자뿐만 아니라 현직 개발자들도 항상 가까이에 두는 ...\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/PqZ1imcTBpI/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/PqZ1imcTBpI/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/PqZ1imcTBpI/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"한빛미디어\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2022-08-29T06:37:58Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"vIBLSTHpXvuO4ld37M5KHZ-dD4I\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLRx0vPvlEmdBjfCADjCc41aD4G0bmdl4R\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2017-03-18T12:25:09Z\",\n" +
                "        \"channelId\": \"UChflhu32f5EUHlY7_SetNWw\",\n" +
                "        \"title\": \"자바 기초 프로그래밍 강좌(Java Programming Tutorial 2017)\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wjLwmWyItWI/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wjLwmWyItWI/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wjLwmWyItWI/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"동빈나\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2017-03-18T12:25:09Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"DF-LH1wDOsOBRgo3cLzUcrZhs1s\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLRx0vPvlEmdDySO3wDqMYGKMVH4Qa4QhR\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2017-03-18T08:57:07Z\",\n" +
                "        \"channelId\": \"UChflhu32f5EUHlY7_SetNWw\",\n" +
                "        \"title\": \"자바(JAVA) 리듬게임 만들기 강좌(How To Make Java Rhythm Game)\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/xs92kqU2YWg/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/xs92kqU2YWg/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/xs92kqU2YWg/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"동빈나\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2017-03-18T08:57:07Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"lHUdGNn8o_qQP-a2eDfkXGpvcPg\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLG7te9eYUi7typZrH4fqXvs4E22ZFn1Nj\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2018-08-02T08:55:35Z\",\n" +
                "        \"channelId\": \"UCkgDFniWXiEGY4SZm0NHf2w\",\n" +
                "        \"title\": \"Do it! 자바 프로그래밍 입문\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oonYQa82MU4/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oonYQa82MU4/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/oonYQa82MU4/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"easyspub\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2018-08-02T08:55:35Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"u4cCtjR0zEzyquGYW9no8-_jdd0\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLz4XWo74AOaeAM5jWwQmrgQ6ccdOKVekw\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-07-05T02:15:00Z\",\n" +
                "        \"channelId\": \"UCFPWMdvwirwSov3dD3UWAhA\",\n" +
                "        \"title\": \"자바 입문\",\n" +
                "        \"description\": \"가장 널리 쓰이는 프로그래밍 언어 Java로 프로그래밍의 기초를 다져보세요. 프로그래머스에서 강의를 시청해보세요. 강의 노트, 실습 ...\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/lmDD5zilk_U/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/lmDD5zilk_U/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/lmDD5zilk_U/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"Programmers\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-07-05T02:15:00Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"LhkN3ocjNIqGwOOp08_rPNkx9SA\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLG7te9eYUi7toebNnbA1cZNRDoUcHmsGd\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2021-04-26T04:55:51Z\",\n" +
                "        \"channelId\": \"UCkgDFniWXiEGY4SZm0NHf2w\",\n" +
                "        \"title\": \"Do it! 자바 완전 정복\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jALoE5BGFls/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jALoE5BGFls/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/jALoE5BGFls/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"easyspub\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2021-04-26T04:55:51Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"iwFkd0UCvZtciQmVwwV3kzvyP8c\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLyebPLlVYXCjs-KSw3HF2jFX9-7S8HjlJ\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2017-03-20T14:34:25Z\",\n" +
                "        \"channelId\": \"UCpW1MaTjw4X-2Y6MwAVptcQ\",\n" +
                "        \"title\": \"예제로 배우는 자바(2018, 무료 JAVA 기초 입문)\",\n" +
                "        \"description\": \"링크: http://cloudstudying.kr/courses/10.\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/c2SaqgdfW1g/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/c2SaqgdfW1g/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/c2SaqgdfW1g/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"홍팍\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2017-03-20T14:34:25Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"KQlQixsWmfz7Xv7rbeyTitPYfQk\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLRYL8FHwJMhCxSLA-T3P9OSBCyMMzfpof\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-08-07T05:46:35Z\",\n" +
                "        \"channelId\": \"UCubIpLB7cA9tWIUZ26WFKPg\",\n" +
                "        \"title\": \"자바 강의\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/j-H7JAooXJ8/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/j-H7JAooXJ8/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/j-H7JAooXJ8/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"스마트인재개발원 인쌤TV\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-08-07T05:46:35Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"JGouaCHq-tC18TS5jM238SRS10Y\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLlTylS8uB2fA5PevGI6ARN6v-VQWmqaix\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-11-23T11:58:44Z\",\n" +
                "        \"channelId\": \"UCO7p2fGIfwVbvk_d6sdfnDw\",\n" +
                "        \"title\": \"Java 기초 강의\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/Dx8fb5eFxBw/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/Dx8fb5eFxBw/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/Dx8fb5eFxBw/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"어라운드 허브 스튜디오 - Around Hub Studio\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-11-23T11:58:44Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"Z84CNQohvp_UnEt_Ps39tQheQss\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLK7AWkPYwus7701xk4hd2O1hKjSmHu5x6\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-02-02T16:38:16Z\",\n" +
                "        \"channelId\": \"UCXiyuCYo4dUqM556XpgqAcQ\",\n" +
                "        \"title\": \"자바(기초문법)\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/L0a6N-rj-CI/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/L0a6N-rj-CI/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/L0a6N-rj-CI/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"coding404\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-02-02T16:38:16Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"njw_w1o4kKJ6BfuqkVXH_qLWUTM\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLv2d7VI9OotTVOL4QmPfvJWPJvkmv6h-2\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-03-31T13:36:02Z\",\n" +
                "        \"channelId\": \"UC_4u-bXaba7yrRz_6x6kb_w\",\n" +
                "        \"title\": \"자바스크립트 기초 강의 (ES5+): 같이 노트를 작성하며 배워요 \uD83D\uDCD2\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wcsVjmHrUQg/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wcsVjmHrUQg/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wcsVjmHrUQg/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"드림코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-03-31T13:36:02Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"PgYGP7yEQsOPBgMLCQOVtKKaBzM\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLuvImYntyp-tiWNGQl22GO1KtVvUKZsZ6\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-05-14T09:35:44Z\",\n" +
                "        \"channelId\": \"UCCKRTqLubHxKwbApXmSf0xg\",\n" +
                "        \"title\": \"초보자를 위한 java programming\",\n" +
                "        \"description\": \"초보자를 위한 java programmimg 책 동영상 강좌.\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/k2QbZCXfD1w/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/k2QbZCXfD1w/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/k2QbZCXfD1w/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"Tartaglia_타르탈리아 TV\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-05-14T09:35:44Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"o1kfE9mIvGzHXby0ZgD2QaJQRBk\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLRIMoAKN8c6O8_VHOyBOhzBCeN7ShyJ27\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2015-08-01T13:50:44Z\",\n" +
                "        \"channelId\": \"UCsOJxLxzQl8IbwGS-Cp5t8w\",\n" +
                "        \"title\": \"모던 자바 (자바 8) - 못다한 이야기\",\n" +
                "        \"description\": \"모던 자바 배우기! 자바 8에 추가된 함수형 기능들에 대해 배워봅시다!\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/mu9XfJofm8U/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/mu9XfJofm8U/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/mu9XfJofm8U/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"케빈 TV\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2015-08-01T13:50:44Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"qzg8blNF87I9uS7IMOwkB9Jln_k\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLz2iXe7EqJONkAP5s_FoUaS8Jy5LZJQrS\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-05-14T06:44:49Z\",\n" +
                "        \"channelId\": \"UC576all-JloCWvzLrc2QZxw\",\n" +
                "        \"title\": \"자바 기초 강의\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/DAm_E28SaFw/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/DAm_E28SaFw/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/DAm_E28SaFw/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"소놀코딩_Sonol Coding\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-05-14T06:44:49Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"4IJ0-MzbZqFbUWxB95xrLAKMqCg\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLKvVQ9ZQzjVkcW5Q2dkij8r0exIxG4sr_\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2022-01-13T02:29:21Z\",\n" +
                "        \"channelId\": \"UCfBvs0ZJdTA43NQrnI9imGA\",\n" +
                "        \"title\": \"자바스크립트 강의\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/1BF_BwW0LPs/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/1BF_BwW0LPs/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/1BF_BwW0LPs/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"코딩알려주는누나\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2022-01-13T02:29:21Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"3sndejVcoM0rXHdY2Hgd85nX-OA\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLW2UjW795-f7XMJRP0c90MIPlDgsbbGQz\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2020-11-29T14:48:08Z\",\n" +
                "        \"channelId\": \"UC1IsspG2U_SYK8tZoRsyvfg\",\n" +
                "        \"title\": \"자바의 정석 3판(자바의 정석 기초편이 최신)\",\n" +
                "        \"description\": \"자바의 정석 3판의 동영상입니다. 최근에 새로 만든 자바의 정석 기초편 강좌를 보시는 것을 권합니다.\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/kyqYaCO9S8U/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/kyqYaCO9S8U/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/kyqYaCO9S8U/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"남궁성의 정석코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2020-11-29T14:48:08Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"yDpVCjATHV3GHOg2cbmWGiLIYNY\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLuHgQVnccGMAb-e41kXPSIpmoz1RvHyN4\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2019-11-11T05:06:14Z\",\n" +
                "        \"channelId\": \"UCvc8kv-i5fvFTJBFAk6n1SA\",\n" +
                "        \"title\": \"JAVA 객체지향 프로그래밍\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/uvYWAfZzb8k/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/uvYWAfZzb8k/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/uvYWAfZzb8k/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"생활코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2019-11-11T05:06:14Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"vo5vAAnM1fC5wbMWvtL0DdMC5hE\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLlV7zJmoG4XI9VguUVNMu3pCjssb4aR_0\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2021-10-28T14:05:13Z\",\n" +
                "        \"channelId\": \"UCthy2IQdYCwResz6uyrPLSg\",\n" +
                "        \"title\": \"프로그래머스 코딩 테스트 - 자바\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/_2yD46UxSso/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/_2yD46UxSso/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/_2yD46UxSso/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"개발자로 취직하기\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2021-10-28T14:05:13Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"SkqtxTjs9fGGOFJodh1oB3aQg_o\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLRx0vPvlEmdCjjaBT_30X8kl534op_Hmy\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2018-12-19T23:22:17Z\",\n" +
                "        \"channelId\": \"UChflhu32f5EUHlY7_SetNWw\",\n" +
                "        \"title\": \"자바(Java)로 이해하는 블록체인 이론과 실습\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wSVN2UdBVuM/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wSVN2UdBVuM/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/wSVN2UdBVuM/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"동빈나\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2018-12-19T23:22:17Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"FioaaXvJqXSLYmerlh4pHJdct6E\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLwXldj55mFgBCqi3HcS2JuTJFGknhXpdf\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2021-07-21T02:02:52Z\",\n" +
                "        \"channelId\": \"UChC561jgj-iqDpWwgr4cPyA\",\n" +
                "        \"title\": \"정보처리기사실기 자바(JAVA) 특강\",\n" +
                "        \"description\": \"정보처리기사실기 합격을 위한 최소한의 자바(JAVA) 특강 입니다. #정보처리기사 #정보처리기사JAVA특강 #정보처리기사강희영 ...\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/FZy3b_v7RJk/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/FZy3b_v7RJk/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/FZy3b_v7RJk/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"두목넷 익스터디\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2021-07-21T02:02:52Z\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"kind\": \"youtube#searchResult\",\n" +
                "      \"etag\": \"_NiLUNRuDzgtq1H7ybhd50RxxGc\",\n" +
                "      \"id\": {\n" +
                "        \"kind\": \"youtube#playlist\",\n" +
                "        \"playlistId\": \"PLFeNz2ojQZjv41Q5cCw8blOpGTTrZS5PU\"\n" +
                "      },\n" +
                "      \"snippet\": {\n" +
                "        \"publishedAt\": \"2021-07-26T11:58:11Z\",\n" +
                "        \"channelId\": \"UCbeOr-FhWmj4KulT8oHup4w\",\n" +
                "        \"title\": \"입문자를 위한 자바스크립트 기초\",\n" +
                "        \"description\": \"\",\n" +
                "        \"thumbnails\": {\n" +
                "          \"default\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/9olX2yyXnVA/default.jpg\",\n" +
                "            \"width\": 120,\n" +
                "            \"height\": 90\n" +
                "          },\n" +
                "          \"medium\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/9olX2yyXnVA/mqdefault.jpg\",\n" +
                "            \"width\": 320,\n" +
                "            \"height\": 180\n" +
                "          },\n" +
                "          \"high\": {\n" +
                "            \"url\": \"https://i.ytimg.com/vi/9olX2yyXnVA/hqdefault.jpg\",\n" +
                "            \"width\": 480,\n" +
                "            \"height\": 360\n" +
                "          }\n" +
                "        },\n" +
                "        \"channelTitle\": \"유노코딩\",\n" +
                "        \"liveBroadcastContent\": \"none\",\n" +
                "        \"publishTime\": \"2021-07-26T11:58:11Z\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    private String getTmpLicenseString(){
        return "{\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"id\": {\n" +
                "                \"kind\": \"youtube#playlist\",\n" +
                "                \"playlistId\": \"PL6i7rGeEmTvqEjTJF3PJR4a1N9KTPpfw0\"\n" +
                "            },\n" +
                "            \"snippet\": {\n" +
                "                \"channelId\": \"UCIimf8pEC9AP0N_9FGW_P_w\",\n" +
                "                \"channelTitle\": \"이기적 영진닷컴\",\n" +
                "                \"description\": \"이 동영상은 [이기적 정보처리기사 필기 절대족보] 도서 내용을 바탕으로 제작되었습니다. 도서 자세히 보기 ...\",\n" +
                "                \"publishedAt\": \"2022-03-15T06:55:57.000Z\",\n" +
                "                \"thumbnails\": {\n" +
                "                    \"default\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/JhKOsZuMDWs/default.jpg\"\n" +
                "                    },\n" +
                "                    \"high\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/JhKOsZuMDWs/hqdefault.jpg\"\n" +
                "                    },\n" +
                "                    \"medium\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/JhKOsZuMDWs/mqdefault.jpg\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"title\": \"정보처리기사 필기 절대족보(2022년)\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": {\n" +
                "                \"kind\": \"youtube#playlist\",\n" +
                "                \"playlistId\": \"PLwXldj55mFgDnGuNrQ99nrvL3Es9htWn9\"\n" +
                "            },\n" +
                "            \"snippet\": {\n" +
                "                \"channelId\": \"UChC561jgj-iqDpWwgr4cPyA\",\n" +
                "                \"channelTitle\": \"두목넷 익스터디\",\n" +
                "                \"description\": \"\",\n" +
                "                \"publishedAt\": \"2022-03-22T05:28:12.000Z\",\n" +
                "                \"thumbnails\": {\n" +
                "                    \"default\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/mWCZfRnGiq8/default.jpg\"\n" +
                "                    },\n" +
                "                    \"high\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/mWCZfRnGiq8/hqdefault.jpg\"\n" +
                "                    },\n" +
                "                    \"medium\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/mWCZfRnGiq8/mqdefault.jpg\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"title\": \"정보처리기사 실기 기출문제(2022)\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": {\n" +
                "                \"kind\": \"youtube#playlist\",\n" +
                "                \"playlistId\": \"PLxpa-X2Ni9I-eEcYibtusx3tsAvzouedA\"\n" +
                "            },\n" +
                "            \"snippet\": {\n" +
                "                \"channelId\": \"UC1odjf0Seqo48PbjVeOUS1w\",\n" +
                "                \"channelTitle\": \"에듀윌 자격증\",\n" +
                "                \"description\": \"\",\n" +
                "                \"publishedAt\": \"2022-12-19T02:32:16.000Z\",\n" +
                "                \"thumbnails\": {\n" +
                "                    \"default\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/_s3Pgwu4tcY/default.jpg\"\n" +
                "                    },\n" +
                "                    \"high\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/_s3Pgwu4tcY/hqdefault.jpg\"\n" +
                "                    },\n" +
                "                    \"medium\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/_s3Pgwu4tcY/mqdefault.jpg\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"title\": \"2023 정보처리기사 필기｜비전공자 눈높이 고난도 개념끝｜에듀윌 EXIT\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": {\n" +
                "                \"kind\": \"youtube#playlist\",\n" +
                "                \"playlistId\": \"PLz95GL3y9Hv2mF7U8D12lf9swtIN6dBPX\"\n" +
                "            },\n" +
                "            \"snippet\": {\n" +
                "                \"channelId\": \"UCZD_mSIrG7VC4Im2lOMZMmQ\",\n" +
                "                \"channelTitle\": \"기사퍼스트 권우석\",\n" +
                "                \"description\": \"\",\n" +
                "                \"publishedAt\": \"2022-01-26T02:15:09.000Z\",\n" +
                "                \"thumbnails\": {\n" +
                "                    \"default\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/ynIBUi1NjwA/default.jpg\"\n" +
                "                    },\n" +
                "                    \"high\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/ynIBUi1NjwA/hqdefault.jpg\"\n" +
                "                    },\n" +
                "                    \"medium\": {\n" +
                "                        \"url\": \"https://i.ytimg.com/vi/ynIBUi1NjwA/mqdefault.jpg\"\n" +
                "                    }\n" +
                "                },\n" +
                "                \"title\": \"[기사퍼스트 X 성안당] 2023 정보처리기사\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"nextPageToken\": \"CAoQAA\",\n" +
                "    \"pageInfo\": {\n" +
                "        \"resultsPerPage\": 10,\n" +
                "        \"totalResults\": 311\n" +
                "    }\n" +
                "}";
    }

    public static class YoutubePlayListMapKey{
        public final static String PLAY_LIST ="PLAY_LIST";
        public final static String PAGE_TOKEN="PAGE_TOKEN";

        private YoutubePlayListMapKey(){};
    }
}
