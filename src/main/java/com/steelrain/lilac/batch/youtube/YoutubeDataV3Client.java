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
    // 호출할때 마다 YouTube 객체를 생성하면 http connection time 발생하므로 static 으로 선언
    private static YouTube YOUTUBE; // 멤버변수 또는 로컬변수로 변경하면 http connection timeout 발생. 유튜브 기본 connection timeout 값은 20000 ms
    private final APIConfig m_apiConfig;

    public YoutubeDataV3Client(APIConfig apiConfig){
        this.m_apiConfig = apiConfig;

        initYoutubeObject();
    }

    // 테스트 할때 할당량을 최대한 적게 쓰기 위해 테스트용도로만 만든 메서드
    public SearchListResponse getSearchListResponse(String keyword){
        try {
            YouTube.Search.List request = YOUTUBE.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setMaxResults(10L)
                    //.setPageToken(StringUtils.hasText(paramToken) ? null : paramToken)
                    //.setOrder("date")
                    .setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/thumbnails/default/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");
            return request.execute();
        } catch (IOException e) {
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
    public Map<String, Object> getYoutubePlayListDTO(String keyword, String paramToken, String[] exclusiveChannels) {
        // 페지지토큰은 따로 검사를 하지 않는다. null 이면 첫페이지 null 이 아니면 다음페이지가 있는것
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
//            String tmp = getTmpLicenseString();
//            String tmp = getTestSubjectListStr();
//            apiResponse = JacksonFactory.getDefaultInstance().fromString(tmp, SearchListResponse.class); // 테스트용 재생목록 객체를 얻기 위한 역직렬화

            // 실제 코드
            YouTube.Search.List request = YOUTUBE.search().list("id,snippet");
            request.setQ(keyword)
                    .setKey(m_apiConfig.getYoutubeKey())
                    .setType("playlist")
                    .setRegionCode("KR")
                    .setMaxResults(m_apiConfig.getPlaylistFetchSize())
                    .setPageToken(StringUtils.hasText(paramToken) ? paramToken : null)
                    //.setOrder("date")
                    .setPublishedAfter(m_apiConfig.getSearchPublishDate())
                    //.setPublishedAfter(DateTime.parseRfc3339("2022-01-01T00:00:00Z")) // The value is an RFC 3339 formatted date-time value (1970-01-01T00:00:00Z).
                    .setFields("items(id/kind,id/playlistId,snippet/channelId,snippet/thumbnails/high/url,snippet/thumbnails/medium/url,snippet/thumbnails/default/url,snippet/title,snippet/publishedAt,snippet/description,snippet/channelTitle),nextPageToken,pageInfo");
            apiResponse = request.execute();

            String keywordResStr = JSON_FACTORY.toString(apiResponse);
            log.debug("\n======== 키워드 검색 결과 json 시작 ==========\n");
            log.debug(keywordResStr);
            log.debug("\n======== 키워드 검색 결과 json 끝 ==========");


            if(apiResponse == null || (apiResponse != null && apiResponse.getItems().size() == 0)){
                Map<String, Object> nullResponseResult = new HashMap<>(2);
                nullResponseResult.put(YoutubePlayListMapKey.PLAY_LIST, new ArrayList(0));
                nullResponseResult.put(YoutubePlayListMapKey.PAGE_TOKEN, null);
                return nullResponseResult;
            }
            int removedCnt = removeExclusiveChannels(exclusiveChannels, apiResponse.getItems());
            log.debug(String.format("\n========== 제거된 채널 수 : %d ============", removedCnt));
            playList = new ArrayList<>(apiResponse.getItems().size());
            for (SearchResult sr : apiResponse.getItems()) {
                if (sr.isEmpty() || !StringUtils.hasText(sr.getId().getPlaylistId())) {
                    continue;
                }
                YoutubePlayListDTO dto = convertToYoutubePlayListDTO(sr);
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

    private int removeExclusiveChannels(String[] channels, List<SearchResult> originList){
        if(channels == null || channels.length == 0){
            return 0;
        }
        Iterator<SearchResult> iterator = originList.iterator();
        int removedCnt = 0;
        while(iterator.hasNext()){
            for (String channel : channels){
                SearchResult sr = iterator.next();
                if(channel.equals(sr.getSnippet().getChannelId())){
                    log.debug(String.format("삭제할 채널 id : %s , 이름 : %s", sr.getSnippet().getChannelId(), sr.getSnippet().getChannelTitle()));
                    iterator.remove();
                    ++removedCnt;
                }
            }
        }
        return removedCnt;
    }
/*
    private Long id;
    public String playListId;
    private Long channelId; // 채널 테이블 FK
    private String title;
    private Timestamp publishDate;
    private String thumbnailMedium;
    private String thumbnailHigh;
    private Integer itemCount;
    private String channelIdOrigin; // API응답에서 반환된 채널ID 문자열
 */
    private YoutubePlayListDTO convertToYoutubePlayListDTO(SearchResult sr){
        YoutubePlayListDTO dto = new YoutubePlayListDTO();
        dto.setPlayListId(sr.getId().getPlaylistId());
        dto.setTitle(sr.getSnippet().getTitle());
        dto.setPublishDate(new Timestamp(sr.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnailMedium(sr.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(sr.getSnippet().getThumbnails().getHigh().getUrl());
        dto.setChannelIdOrigin(sr.getSnippet().getChannelId());
        // itemCount는 재생목록의 영상들을 가져와야 알 수 있음.
        return dto;
    }

    /*
        재생목록에 몇개의 영상들이 있는지는 일단 한번 호출해봐야 알 수 있기 때문에 do while 로 루프를 돈다
        비공개 영상은 영상정보가 없기 때문에 걸러낸다
     */
    @Override
    public List<YoutubeVideoDTO> getVideoDTOListByPlayListId(String playListId){
        if(!StringUtils.hasText(playListId) || YOUTUBE == null){
            return new ArrayList<>(0);
        }
        // 재생목록에 중복된 영상이 들어갈 수 있므로 Map에 저장했다가 밸류값들만 List로 리턴한다
        Map<String, YoutubeVideoDTO> videoMap = null;
        String pageToken = null;
        boolean isExit = true;
        int cnt = 0;
        int pageCnt = 0;
        // 영상의 상세정보 API의 파라미터로 넘길 "videoId,videoId,videoId..." 문자열을 만들어야 한다
        StringBuilder videoIdBuilder = new StringBuilder(600); // 유튜브 video id 개수 * API 처리가능 ID 개수 + 콤마의 개수 == 11 * 50 + 49 , 넉넉하게 600으로 잡는다...
        try {
            do{
                //YouTube.PlaylistItems.List plRequest = YOUTUBE.playlistItems().list("id,snippet,contentDetails,status");
                YouTube.PlaylistItems.List plRequest = YOUTUBE.playlistItems().list("id,snippet,status"); // 따로 상세정보 조회를 하기때문에 contentDetails 는 필요없음
                PlaylistItemListResponse plResponse = plRequest.setMaxResults(50L) // 한번에 최대 50개 까지만 지원
                        .setPlaylistId(playListId)
                        .setKey(m_apiConfig.getYoutubeKey())
                        .setPageToken(StringUtils.hasText(pageToken) ? pageToken : null)
                        .execute();
                if(pageCnt == 0){ // 처음 한번 호출할때만 초기화 작업을 한다
                    int totalResults = plResponse.getPageInfo().getTotalResults();
                    pageCnt = totalResults / 50;  //  재생목록 looping count 계산시작 : 재생목록의 영상개수가 50개가 넘어가면 루프를 돌아야 하므로 몇번이나 돌아야 하는지 계산한다
                    pageCnt = pageCnt + ((totalResults % 50) > 0  ? 1 : 0); // 나머지를 구해서 한번더 돌아야 하는지 검사
                    videoMap = new HashMap<>(totalResults);

                    log.debug(String.format("\n전체 영상개수 : %d", totalResults));
                    log.debug(String.format("\n페이징할 페이지개수 : %d", pageCnt));
                }// 재생목록 looping count 계산 끝
                List<PlaylistItem> items = plResponse.getItems(); // 영상의 상세정보를 얻기 위한 파라미터 만들기 시작
                for(int i=0, size=items.size()-1 ; i <= size ; i++){
                    PlaylistItem item = items.get(i);
                    if(validateNullablePlaylistItem(item)){ // 영상정보가 null 인것, 비공개영상들을 걸러낸다
                        continue;
                    }
                    String videoId = item.getSnippet().getResourceId().getVideoId();
                    videoIdBuilder.append(videoId);
                    if(i == size){
                        continue;
                    }
                    videoIdBuilder.append(",");
                } // 영상의 상세정보를 얻기 위한 파라미터 만들기 끝
                // 재생목록에 있는 영상정보는 부족하므로 videoId 로 영상의 자세한정보를 API 호출로 가져온다. 영상의 상세정보를 API로 얻어오기 시작
                VideoListResponse videoDetailResponse = getVideoDetail(videoIdBuilder.toString());
                videoIdBuilder.setLength(0);
                if(validateNullableVideoDetailResponse(videoDetailResponse)){ // 영상목록이 null 이면 true, 아니면 false
                    continue;
                }
                List<Video> videoList = videoDetailResponse.getItems();
                for (Video video : videoList) {
                    log.debug(String.format("\n========= 재생목록의 영상정보를 가져와서 초기화 시작 - 재생목록id : %s , 영상id : %s", playListId, video.getId()));
                    //log.debug(String.format("\n========= 상세정보 가져오기 이전의 영상정보 - id : %s , toString : %s", video.getId(), video.toPrettyString()));
                    Optional<YoutubeVideoDTO> dto = convertVideoToDTO(video, playListId);
                    if(dto.isPresent() && !videoMap.containsKey(video.getId())) { // 중복된 영상인가? 유튜브 재생목록에 가끔씩 같은영상이 중복되어 올라온다
                        videoMap.put(video.getId(), dto.get());
                    }
                }// 영상의 상세정보를 API로 얻어오기 끝
                if(cnt < pageCnt){ // 페이징을 해야할지 안할지 체크해서 페이지가 남아있으면 페이징토큰을 얻어오고 아니면 1번만 돌고 종료
                    pageToken = plResponse.getNextPageToken();
                }else{
                    isExit = false;
                }
                ++cnt;
            } while(isExit);
        } catch (IOException e) {
            log.error(String.format("유튜브 재생목록의 영상조회 도중 예외 발생 - playListId : %s", playListId), e);
            //throw new LilacYoutubeAPIException("유튜브 재생목록의 영상조회 도중 예외 발생", e, playListId);
            if(videoMap.size() == 0){
                return new ArrayList<>(0);
            }
        }
        log.debug(String.format("\n===== 재생목록 : %s 의 영상갯수 : %d : ", playListId, videoMap.values().size()));
        return new ArrayList<>(videoMap.values());
    }

    private Optional<YoutubeVideoDTO> convertVideoToDTO(Video video, String playlistId){
        if(video.getSnippet() == null || video.getContentDetails() == null || video.getStatistics() == null){
            return Optional.empty();
        }
        YoutubeVideoDTO dto = new YoutubeVideoDTO();
        dto.setVideoId(video.getId());
        dto.setTitle(video.getSnippet().getTitle());
        dto.setPlaylistId(playlistId);
        dto.setPublishDate(new Timestamp(video.getSnippet().getPublishedAt().getValue()));
        dto.setThumbnailDefault(video.getSnippet().getThumbnails().getDefault().getUrl());
        dto.setThumbnailMedium(video.getSnippet().getThumbnails().getMedium().getUrl());
        dto.setThumbnailHigh(video.getSnippet().getThumbnails().getHigh().getUrl());

        dto.setViewCount(video.getStatistics().getViewCount() == null ? 0 : video.getStatistics().getViewCount().longValue());
        dto.setDescription(video.getSnippet().getDescription());
        dto.setLikeCount(video.getStatistics().getLikeCount() == null ? 0 : video.getStatistics().getLikeCount().longValue());
        dto.setFavoriteCount(video.getStatistics().getFavoriteCount() == null ? 0 : video.getStatistics().getFavoriteCount().longValue());
        boolean isCommentDisabled = validateDisableCommentVideo(video);
        dto.setCommentDisabled(isCommentDisabled);
        dto.setCommentCount(isCommentDisabled ? 0 : video.getStatistics().getCommentCount().longValue());
        dto.setDuration(video.getContentDetails().getDuration());

        return Optional.of(dto);
    }

    // 유튜브API를 통해 유튜브 영상의 댓글리스트를 가져온다.
    @Override
    public List<YoutubeCommentDTO> getCommentList(String videoId) {
        if(!StringUtils.hasText(videoId) || YOUTUBE == null){
            return new ArrayList<>(0);
        }
        List<YoutubeCommentDTO> commentList = null;
        try {
            YouTube.CommentThreads.List request = YOUTUBE.commentThreads()
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
                if(thread.getSnippet() == null ||
                   thread.getSnippet().getTopLevelComment() == null ||
                   thread.getSnippet().getTopLevelComment().getSnippet() == null){
                    continue;
                }
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
        } catch (IOException e) {
            if(commentList == null){
                commentList = new ArrayList<>(0);
            }
            log.error(String.format("영상의 댓글리스트 가져오기 예외 - videoId : %s , 에러정보 : %s", videoId, e.getMessage()), e);
        }
        return commentList;
    }

    @Override
    public Optional<YoutubeChannelDTO> getChannelInfo(String channelId) {
        if(!StringUtils.hasText(channelId) || YOUTUBE ==null){
            return Optional.empty();
        }
        // 테스트 채널 id :  UCZD_mSIrG7VC4Im2lOMZMmQ
        YoutubeChannelDTO resultDTO = null;
        try {
            YouTube.Channels.List request = YOUTUBE.channels()
                    .list("brandingSettings,contentDetails,id,snippet,statistics,status,topicDetails");
            ChannelListResponse response = request.setKey(m_apiConfig.getYoutubeKey()).setId(channelId).execute();
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
        } catch (IOException e) {
            throw new LilacYoutubeAPIException("채널 정보 가져오기 예외", e, channelId);
        }
        return Optional.ofNullable(resultDTO);
    }

    /*
     재생목록에 있는 영상정보의 유효성 체크, null 이면 true 아니면 false
     검증 순서 : 비공개 영상인지 체크, snippet, status 속성이 있는지,
     */
    private boolean validateNullablePlaylistItem(PlaylistItem item){
        return !"public".equals(item.getStatus().getPrivacyStatus()) ||
                item.getStatus() == null ||
                item.getSnippet() == null;
    }

    // 영상의 상제정보의 유효성 체크, 영상목록이 없으면 true , 영상목록이 있으면 false
    private boolean validateNullableVideoDetailResponse(VideoListResponse videoInfo){
        return videoInfo.getItems() == null || videoInfo.getItems().size() == 0;
    }

    // 댓글금지 영상인지 검사하는 메서드, 댓글금지 이면 true, 댓글금지가 아니면 false
    private boolean validateDisableCommentVideo(Video video) {
        return video.getStatistics() == null || video.getStatistics().getCommentCount() == null;
    }

    // 유튜브API의 클라이언트 객체를 초기화
    private static void initYoutubeObject() {
        try {
            YOUTUBE = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("lilac").build();
        }catch(GeneralSecurityException gse){
            log.error("YouTube 객체 초기화 도중 예외 발생 : {}", gse);
        }catch (IOException ioe){
            log.error("YouTube 객체 초기화 도중 예외 발생 : {}", ioe);
        }
    }

    // 유튜브 영상의 상세정보를 반환한다
    private VideoListResponse getVideoDetail(String videoId){
        log.debug(String.format("\n========= getVideoDetail 파라미터 : %s", videoId));
        try {
            YouTube.Videos.List request = YOUTUBE.videos()
                    .list("snippet,contentDetails,statistics");
            return request.setId(videoId).setKey(m_apiConfig.getYoutubeKey()).execute();

        } catch (IOException e) {
            throw new LilacYoutubeAPIException("유튜브 재생목록 영상의 상제정보조회 도중 예외 발생", e, videoId);
        }
    }

    public static class YoutubePlayListMapKey{
        public final static String PLAY_LIST ="PLAY_LIST";
        public final static String PAGE_TOKEN="PAGE_TOKEN";

        private YoutubePlayListMapKey(){};
    }
}
