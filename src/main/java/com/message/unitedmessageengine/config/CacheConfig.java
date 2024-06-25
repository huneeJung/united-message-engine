package com.message.unitedmessageengine.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Value("${cache.size:100}")
    private Integer cacheSize;

    @Value("${cache.expireTime:1800000}")
    private Integer cacheExpireTime;

    @Bean
    public Cache<Long, byte[]> imageCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(cacheExpireTime, TimeUnit.MILLISECONDS)
                .build();
    }

}
