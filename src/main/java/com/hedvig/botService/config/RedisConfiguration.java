package com.hedvig.botService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfiguration {

  @Bean
  GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(
    ObjectMapper objectMapper) {
    return new GenericJackson2JsonRedisSerializer(objectMapper);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
    RedisConnectionFactory jedisConnectionFactory,
    GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer) {
    val redisTemplate = new RedisTemplate<String, Object>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory);
    redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
    return redisTemplate;
  }
}
