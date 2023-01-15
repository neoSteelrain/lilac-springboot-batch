package com.steelrain.lilac.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TestJobConfiguraion {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job testJob(){
        return jobBuilderFactory.get(LocalDateTime.now().toString())
                .start(testStep2())
                //.incrementer(new RunIdIncrementer())
                .build();
    }

    /*@Bean
    public Step testStep1(){
        return stepBuilderFactory.get("testStep10")
                .tasklet((contribution, chunkContext) -> {
                    log.debug("-----------> 이것이 스텝1 !");
                    return RepeatStatus.FINISHED;
                }).build();
    }*/


    @Bean
    public Step testStep2(){
        return stepBuilderFactory.get("step2")
                .tasklet(testTasklet()).build();
    }

    @Bean
    public Tasklet testTasklet(){
        return ((contribution, chunkContext) -> {
            /*String name = (String) chunkContext.getStepContext()
                    .getJobParameters().get("name");*/
            //chunkContext.getStepContext().getJobParameters().keySet().stream().forEach(key -> System.out.println("--------> key : " + key));
            String name = chunkContext.getStepContext().getJobName();
            log.debug("-----------> job name 값 : " + name);
            return RepeatStatus.FINISHED;
        });
    }
}
