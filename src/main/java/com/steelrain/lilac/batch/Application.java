package com.steelrain.lilac.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@EnableBatchProcessing
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        /*SpringApplication application = new SpringApplication(Application.class);

        Properties properties = new Properties();
        properties.put("spring.batch.job.enable", true);
        application.run(args);*/
    }

}
