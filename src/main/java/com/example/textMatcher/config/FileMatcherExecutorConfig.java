package com.example.textMatcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class FileMatcherExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService fileMatcherExecutor(
            FileMatcherProperties properties
    ) {
        return Executors.newFixedThreadPool(
                properties.maxParallelFiles()
        );
    }
}
