package net.danil.web.statistics;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
public class StatisticsConfiguration {
    @Bean(name = "generalStatisticsCacheManager")
    @Primary
    public CacheManager generalStatisticsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("statistics/general");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }

    @Bean(name = "personalStatisticsCacheManager")
    public CacheManager personalStatisticsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("statistics/personal");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
