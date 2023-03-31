package com.steelrain.lilac.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;


@Slf4j
public class YoutubeJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("========== 유튜브 배치 시작 ==========");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("========== 유튜브 배치 종료 ==========");
    }
}
