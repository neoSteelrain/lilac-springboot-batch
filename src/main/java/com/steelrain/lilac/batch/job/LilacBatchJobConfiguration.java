package com.steelrain.lilac.batch.job;

import com.steelrain.lilac.batch.tasklet.YoutubeTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
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
                .start(youtubeFetchInitStep())
                .next(youtubeFetchRunStep())
                .next(youtubeFetchFinishStep())
                .build();
    }

    @Bean
    public Step youtubeFetchInitStep(){
        return m_stepBuilderFactory.get("youtube_fetch_init_step")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("========== 유튜브 배치 시작 ==========");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public Step youtubeFetchRunStep(){
        return m_stepBuilderFactory.get("youtube_fetch_run_step")
                .tasklet(m_youtubeTasklet)
                .build();
    }

    @Bean
    public Step youtubeFetchFinishStep(){
        return m_stepBuilderFactory.get("youtube_fetch_finish_step")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("========== 유튜브 배치 종료 ==========");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }
}
