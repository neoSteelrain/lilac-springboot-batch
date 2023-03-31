package com.steelrain.lilac.batch.job;

import com.steelrain.lilac.batch.listener.YoutubeJobListener;
import com.steelrain.lilac.batch.tasklet.YoutubeTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class LilacBatchJobConfiguration {

    private final JobBuilderFactory m_jobBuilderFactory;
    private final StepBuilderFactory m_stepBuilderFactory;
    private final YoutubeTasklet m_youtubeTasklet;


    @Bean
    public Job youtubeJob(){
        return m_jobBuilderFactory.get(LocalDateTime.now().toString())
                .start(youtubeFetchRunStep())
                .listener(new YoutubeJobListener())
                .build();
    }

    @Bean
    public Step youtubeFetchRunStep(){
        return m_stepBuilderFactory.get("youtube_fetch_run_step")
                .tasklet(m_youtubeTasklet)
                .build();
    }
}
