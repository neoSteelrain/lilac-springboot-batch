package com.steelrain.lilac.batch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:api-keys.properties")
public class APIConfiguration {

    @Value("${youtube.api.key}")
    private String m_youtubeKey;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    APIConfig apiConfig(){
        return new APIConfig(this.m_youtubeKey);
    }
}
