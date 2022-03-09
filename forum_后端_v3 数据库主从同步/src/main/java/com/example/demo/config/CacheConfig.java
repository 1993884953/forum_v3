package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;


@Configuration
public class CacheConfig {
    private static final int DEFAULT_MAXSIZE = 50000;  // 缺省最大容量50000
    private static final int DEFAULT_TTL = 10;         // 缺省10秒

    /**
     * 定义cache名称、过期时间（秒）、最大容量
     * 每个cache缺省：10秒超时、最多缓存50000条数据，需要修改可以在构造方法的参数中指定
     */
    public enum Caches {
        bookCache(60, 50),           // 图书缓存(60秒，最大缓存条数50)
        findUserByToken(60, 10),    // 通过token查用户，缓存30秒，最大容量100条
        categories(3600,100),        //帖子分类
        listPost(60,1000),           //帖子列表
        getVisitor(60,5),
        getTagOrder(60, 10),
        categoriesById(60,10),

        ;


        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        private int maxSize = DEFAULT_MAXSIZE;    // 最大數量
        private int ttl = DEFAULT_TTL;            // 过期时间（秒）

        public int getMaxSize() {
            return maxSize;
        }

        public int getTtl() {
            return ttl;
        }
    }

    /**
     * 创建基于 Redis 的 Cache Manager
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(DEFAULT_TTL)));

        for (Caches c : Caches.values()) {
            builder.withCacheConfiguration(c.name(), RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(c.getTtl())));
        }

        return builder.build();
    }


}