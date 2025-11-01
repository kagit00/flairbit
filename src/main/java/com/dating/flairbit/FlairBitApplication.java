package com.dating.flairbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.dating.flairbit.repo")
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class FlairBitApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlairBitApplication.class, args);
	}

}
