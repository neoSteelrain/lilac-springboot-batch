package com.steelrain.lilac.batch.mapper;

import com.steelrain.lilac.batch.datamodel.KeywordLicenseDTO;
import com.steelrain.lilac.batch.datamodel.KeywordSubjectDTO;
import com.steelrain.lilac.batch.mapper.KeywordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

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
            System.out.println(subject.getId());
            System.out.println(subject.getNameKr());
            System.out.println(subject.getNameEng());
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
        });
        System.out.println("====================================");
    }
}
