package com.steelrain.lilac.batch.domain;

import com.steelrain.lilac.batch.datamodel.YoutubeChannelDTO;
import com.steelrain.lilac.batch.datamodel.YoutubePlayListDTO;
import com.steelrain.lilac.batch.exception.LilacBatchException;
import com.steelrain.lilac.batch.exception.LilacNoSushChannelnfoException;
import com.steelrain.lilac.batch.exception.LilacYoutubeDomainException;
import com.steelrain.lilac.batch.repository.IYoutubeRepository;
import com.steelrain.lilac.batch.youtube.IYoutubeClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 유튜브 채널정보를 관리하는 클래스
 * - DB에서 채널정보 읽기
 * - 채널정보 저장
 * - 재생목록에 채널정보 설정
 */
@Slf4j
@Component
public class YoutubeChannelManager {
    private final IYoutubeClient m_youtubeClient;
    private final IYoutubeRepository m_youtubeRepository;
    private static Map<String, YoutubeChannelDTO> m_chnDTOMap;


    public YoutubeChannelManager(IYoutubeClient youtubeClient, IYoutubeRepository repository){
        this.m_youtubeClient = youtubeClient;
        this.m_youtubeRepository = repository;

        loadYoutubeChannelInfo();
    }

    /*
       - 재생목록의 채널정보를 API를 통해 가져온다
       - 채널정보를 DB에 저장하고 id PK 값을 얻는다
       - DB에서 가져온 id PK값을 재생목록의 채널id FK로 설정해준다
     */
    public void initPlayList(List<YoutubePlayListDTO> playLists){
        try{
            for(YoutubePlayListDTO pl : playLists){
                if(m_chnDTOMap.containsKey(pl.getChannelIdOrigin())){
                    pl.setChannelId( m_chnDTOMap.get( pl.getChannelIdOrigin() ).getId() );
                }else{
                    Optional<YoutubeChannelDTO> channelDTO = m_youtubeClient.getChannelInfo(pl.getChannelIdOrigin());
                    if(!channelDTO.isPresent()){
                        throw new IllegalArgumentException(String.format("채널정보를 유튜브 API를 통해 불러올 수 없음 : 채널 ID - %s", pl.getChannelIdOrigin()));
                    }
                    YoutubeChannelDTO originChnDTO = channelDTO.get();
                    m_youtubeRepository.saveChannel(originChnDTO);
                    pl.setChannelId(originChnDTO.getId());
                    m_chnDTOMap.put(pl.getChannelIdOrigin(), originChnDTO);
                }
            }
        }catch(LilacBatchException be){
            throw new LilacYoutubeDomainException(String.format("재생목록에 채널ID설정 중 예외 발생, 재생목록정보 - %s", playLists.toString()), be);
        }
    }

    private void loadYoutubeChannelInfo(){
        List<YoutubeChannelDTO> chnList = m_youtubeRepository.findAllYoutubeChannels();
        m_chnDTOMap = new HashMap<>(chnList.size() * 2);
        for (YoutubeChannelDTO channelDTO : chnList) {
            m_chnDTOMap.put(channelDTO.getChannelId(), channelDTO);
        }
    }

    public Optional<Long> getId(String channelId){
        if(!StringUtils.hasText(channelId) || m_chnDTOMap == null){
            return Optional.empty();
        }
        if(!m_chnDTOMap.containsKey(channelId)){
            throw new LilacNoSushChannelnfoException(String.format("찾을 수 없는 채널정보입니다. - channelId : %s", channelId));
        }
        return  Optional.ofNullable(m_chnDTOMap.get(channelId).getId());
    }

    public void clear(){
        if(m_chnDTOMap == null){
            return;
        }
        m_chnDTOMap.clear();
    }
}
