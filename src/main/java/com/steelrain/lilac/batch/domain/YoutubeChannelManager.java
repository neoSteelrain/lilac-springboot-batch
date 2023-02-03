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
import java.util.stream.Collectors;

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
    public void initManager(List<YoutubePlayListDTO> playLists){
        try{
            for(YoutubePlayListDTO dto : playLists){
                if(m_chnDTOMap.containsKey(dto.getChannelIdOrigin())){
                    dto.setChannelId( m_chnDTOMap.get( dto.getChannelIdOrigin() ).getId() );
                }else{
                    Optional<YoutubeChannelDTO> channelDTO = m_youtubeClient.getChannelInfo(dto.getChannelIdOrigin());
                    if(!channelDTO.isPresent()){
                        continue;
                    }
                    YoutubeChannelDTO originChnDTO = channelDTO.get();
                    m_youtubeRepository.saveChannel(originChnDTO);
                    dto.setChannelId(originChnDTO.getId());
                    m_chnDTOMap.put(dto.getChannelIdOrigin(), originChnDTO);
                }
            }
        }catch(LilacBatchException be){
            throw new LilacYoutubeDomainException(String.format("채널관리자 초기화중 예외 발생"), be);
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
