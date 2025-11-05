package com.dating.flairbit.config;

import com.dating.flairbit.dto.MatchSuggestionDTO;
import com.dating.flairbit.config.factory.MatchSuggestionResponseFactory;
import com.dating.flairbit.config.factory.ResponseFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Beans {

    @Bean
    public ResponseFactory<MatchSuggestionDTO> matchResponseFactory() {
        return new MatchSuggestionResponseFactory();
    }

    @Bean(name = "usersExportExecutor")
    public ThreadPoolTaskExecutor usersExportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("UsersExport-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(runnable -> runnable);
        executor.initialize();
        return executor;
    }

    @Bean
    public ExecutorService matchSuggestionsImportExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("io-executor-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                4, 8, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(500),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        super.rejectedExecution(r, e);
                    }
                }
        );
        executor.allowCoreThreadTimeOut(false);
        return executor;
    }

    @Bean
    public ExecutorService ioExecutor() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("io-executor-%d").build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                8, 16, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        super.rejectedExecution(r, e);
                    }
                }
        );
        executor.allowCoreThreadTimeOut(false);
        return executor;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    @Bean(name = "usersDumpExecutor")
    public ThreadPoolTaskExecutor usersDumpExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("UsersDump-");
        executor.setRejectedExecutionHandler((r, e) -> {
            log.error("Task rejected for executor {}: queue size={}", executor.getThreadNamePrefix(), e.getQueue().size());
            throw new RejectedExecutionException("UsersDump executor saturated");
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Bean(name = "matchFetchExecutor")
    public Executor matchFetchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("MatchFetchExecutor-");
        executor.initialize();
        return executor;
    }

}
