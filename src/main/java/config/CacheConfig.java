package config;

import cache.LRUCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Value("${cache.capacity:100}")
    private int capacity;

    @Value("${cache.default-ttl-ms:60000}")
    private long defaultTtlMs;

    @Value("${cache.ttl-check-interval-ms:10000}")
    private long ttlCheckIntervalMs;

    @Bean
    public LRUCache lruCache(){
        return new LRUCache(capacity, defaultTtlMs, ttlCheckIntervalMs);
    }
}
