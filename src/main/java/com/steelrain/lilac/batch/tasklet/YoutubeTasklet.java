package com.steelrain.lilac.batch.tasklet;

import com.steelrain.lilac.batch.domain.YoutubeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YoutubeTasklet implements Tasklet {

    private final YoutubeManager m_mgr;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        m_mgr.doYoutubeBatch();
        return RepeatStatus.FINISHED;
    }
}
