package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import com.steelrain.lilac.batch.datamodel.SubjectBatchResultDTO;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

@Slf4j
@SpringBootTest
public class MapperTests {

    @Autowired
    private KeywordMapper keywordMapper;

    @Test
    public void testGetSubjectList(){
        List<KeywordSubjectDTO> list = keywordMapper.getSubjectList();

        assertThat(list != null);

        System.out.println("====================================");
        list.stream().forEach(subject -> {
            log.debug(subject.getId().toString());
            log.debug(subject.getName());
            log.debug(subject.getKeyWord());
            log.debug(subject.getPageToken());
            log.debug(subject.getUpdateTime().toString());

        });
        System.out.println("====================================");
    }

    @Test
    public void testGetLicenseList(){
        List<KeywordLicenseDTO> list = keywordMapper.getLicenseList();

        assertThat(list != null);

        System.out.println("====================================");
        list.stream().forEach(license -> {
            System.out.println(license.getCode());
            System.out.println(license.getName());
            System.out.println(license.getPageToken());
            System.out.println(license.getUpdateTime());
        });
        System.out.println("====================================");
    }

    @Test
    public void testUdpateSubjectPageToken(){
        SubjectBatchResultDTO dto = SubjectBatchResultDTO.builder()
                .id(1)
                .pageToken("TEST-TOKEN")
                .build();
        int cnt = keywordMapper.udpateSubjectPageToken(dto);
        assertThat(cnt > 0);
    }
}
