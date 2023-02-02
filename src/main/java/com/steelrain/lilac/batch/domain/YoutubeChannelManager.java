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
    }

    /*
       - 재생목록의 채널정보를 API를 통해 가져온다
       - 채널정보를 DB에 저장하고 id PK 값을 얻는다
       - DB에서 가져온 id PK값을 재생목록의 채널id FK로 설정해준다
     */
    public void initManager(List<YoutubePlayListDTO> playLists){
        m_chnDTOMap = new HashMap<>(playLists.size()); // 중복된 채널을 걸러내기 위해 map을 사용한다
        try{
            for(YoutubePlayListDTO dto : playLists){
                if(m_chnDTOMap.containsKey(dto.getChannelIdOrigin())){
                    continue;
                }
                Optional<YoutubeChannelDTO> channelDTO = m_youtubeClient.getChannelInfo(dto.getChannelIdOrigin());
                if(!channelDTO.isPresent()){
                    continue;
                }
                m_chnDTOMap.put(dto.getChannelIdOrigin(), channelDTO.get());
            }
        }catch(LilacBatchException be){
            throw new LilacYoutubeDomainException(String.format("채널관리자 초기화중 예외 발생"), be);
        }
        List<YoutubeChannelDTO> chnList = new ArrayList<>(m_chnDTOMap.values());


//        log.debug("\n==================== 업데이트 이전 채널정보 시작 ==================");
//        m_chnDTOMap.values().stream().forEach(dto -> {
//            log.debug(String.format("채널 id : %d , 채널 타이틀 : %s",dto.getId(), dto.getTitle()));
//        });
//        log.debug("\n==================== 업데이트 이전 채널정보 끝 ==================");

        // 채널의 id는 DB에서 자동증가이므로 insert 이후에는 map 에도 id 값이 들어가 있다
        m_youtubeRepository.saveChannelList(chnList);

//        log.debug("\n==================== 업데이트된 채널정보 시작 ==================");
//        m_chnDTOMap.values().stream().forEach(dto -> {
//            log.debug(String.format("채널 id : %d , 채널 타이틀 : %s",dto.getId(), dto.getTitle()));
//        });
//        log.debug("\n==================== 업데이트된 채널정보 끝 ==================");

//        List<String> collect = m_chnDTOMap.values().stream().map(YoutubeChannelDTO::getChannelId).collect(Collectors.toList());
//        collect.stream().forEach(System.out::println);
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
}
