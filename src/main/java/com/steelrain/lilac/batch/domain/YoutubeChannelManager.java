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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class YoutubeChannelManager {
    private final IYoutubeClient m_youtubeClient;
    private final IYoutubeRepository m_youtubeRepository;
    private Map<String, YoutubeChannelDTO> m_chnDTOMap;


    public YoutubeChannelManager(IYoutubeClient youtubeClient, IYoutubeRepository repository){
        this.m_youtubeClient = youtubeClient;
        this.m_youtubeRepository = repository;
    }

    /*
       - 재생목록의 채널정보를 API를 통해 가져온다
       - 채널정보를 DB에 저장하고 id PK 값을 얻는다
       - DB에서 가져온 id PK값을 재생목록의 채널id FK로 설정해준다
     */
    public void initManager(List<YoutubePlayListDTO> playLists){
        // 채널목록만들기
        m_chnDTOMap = new HashMap<>(playLists.size());
        try{
            for(YoutubePlayListDTO dto : playLists){
                if(m_chnDTOMap.containsKey(dto.getChannelId())){
                    continue;
                }
                m_chnDTOMap.put(dto.getChannelIdOrigin(), m_youtubeClient.getChannelInfo(dto.getChannelIdOrigin()));
            }
        }catch(LilacBatchException be){
            throw new LilacYoutubeDomainException(String.format("채널관리자 초기화중 예외 발생"), be);
        }
        List<YoutubeChannelDTO> chnList = new ArrayList<>(m_chnDTOMap.values());

        log.debug("\n==================== 업데이트이전 채널정보 시작 ==================");
        m_chnDTOMap.values().stream().forEach(dto -> {
            log.debug(dto.toString());
        });
        log.debug("\n==================== 업데이트이전 채널정보 끝 ==================");

        m_youtubeRepository.saveChannelList(chnList);
        // 저장된 id를 가져와서 다시 map에 있는 채널정보에 업데이트
        List<YoutubeChannelDTO> updatedChnList = m_youtubeRepository.getChannelList(new ArrayList<String>(m_chnDTOMap.keySet()));
        if(m_chnDTOMap.keySet().size() != updatedChnList.size()){
            log.error(String.format("유튜브 API에서 가져온 채널정보와 DB에 저장된 채널정보가 일치하지 않습니다 - API 채널갯수 : %s , DB 채널갯수 : %s", m_chnDTOMap.keySet().size(), updatedChnList.size()));
            throw new LilacYoutubeDomainException(String.format("유튜브 API에서 가져온 채널정보와 DB에 저장된 채널정보가 일치하지 않습니다 - API 채널갯수 : %s , DB 채널갯수 : %s", m_chnDTOMap.keySet().size(), updatedChnList.size()));
        }
        for(YoutubeChannelDTO chnDbDTO : updatedChnList){
            YoutubeChannelDTO tmp = m_chnDTOMap.get(chnDbDTO.getChannelId());
            tmp.setId(chnDbDTO.getId());
        }

        log.debug("\n==================== 업데이트된 채널정보 시작 ==================");
        m_chnDTOMap.values().stream().forEach(dto -> {
            log.debug(dto.toString());
        });
        log.debug("\n==================== 업데이트된 채널정보 끝 ==================");
    }

    public Long getId(String channelId){
        if(!StringUtils.hasText(channelId) || m_chnDTOMap == null){
            return null;
        }
        if(!m_chnDTOMap.containsKey(channelId)){
            throw new LilacNoSushChannelnfoException(String.format("찾을 수 없는 채널정보입니다. - channelId : %s", channelId));
        }
        return m_chnDTOMap.get(channelId).getId();
    }
}
