package ru.stm.shcherbinki3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.util.pagination.PageResponse;

@Configuration
public class RedisConfig {

    // -------------------------
    // 1. Дефолтный Redis для JwtServiceRedis
    // -------------------------
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.redis")
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(@Qualifier("redisProperties") RedisProperties properties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(properties.getHost(),
                                                                               properties.getPort());
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }

    // -------------------------
    // 2. Redis для кеша (порт 6380)
    // -------------------------
    @Bean(name = "cacheRedisProperties")
    @ConfigurationProperties(prefix = "spring.cache.redis")
    public RedisProperties cacheRedisProperties() {
        return new RedisProperties();
    }

    @Bean(name = "cacheRedisConnectionFactory")
    public LettuceConnectionFactory cacheRedisConnectionFactory(
            @Qualifier("cacheRedisProperties") RedisProperties properties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(properties.getHost(),
                                                                               properties.getPort());
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisCacheManager cacheManager(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Qualifier("cacheRedisProperties") RedisProperties properties) {

        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getTimeout()) // или Duration.ofMinutes(10)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    // -------------------------
    // ObjectMapper с JavaTimeModule
    // -------------------------
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Для поддержки Java 8 Date/Time API, если используется
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // Включаем информацию о типах
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, PageResponse<TicketPublicDto>> pageResponseRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        // Создаем копию ObjectMapper специально для Redis, если нужно кастомизировать дальше
        ObjectMapper redisObjectMapper = objectMapper.copy();

        RedisTemplate<String, PageResponse<TicketPublicDto>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        // Используем GenericJackson2JsonRedisSerializer для поддержки generics и динамических типов
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

}
