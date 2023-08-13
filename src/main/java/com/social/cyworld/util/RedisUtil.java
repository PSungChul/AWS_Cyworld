package com.social.cyworld.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
//    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    RedisTemplate<String, Object> redisBlackListTemplate;
//    private final RedisTemplate<String, Object> redisBlackListTemplate;
    @Autowired
    RedisTemplate<String, Object> redisInvalidationTemplate;
//    private final RedisTemplate<String, Object> redisInvalidationTemplate;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////RefreshToken - 로그인 유지 키
    public void set(String key, Object o, int minutes) {
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisTemplate.opsForValue().set(key, o, minutes, TimeUnit.MINUTES);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void update(String key, String newKey) {
        redisTemplate.rename(key, newKey);
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////BlackList - 로그아웃
    public void setBlackList(String key, Object o, int minutes) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, minutes, TimeUnit.MINUTES);
    }

    public Object getBlackList(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

    public boolean deleteBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.delete(key));
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Invalidation - 다중 로그인
public void setInvalidation(String key, Object o, int minutes) {
    redisInvalidationTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
    redisInvalidationTemplate.opsForValue().set(key, o, minutes, TimeUnit.MINUTES);
}

    public Object getInvalidation(String key) {
        return redisInvalidationTemplate.opsForValue().get(key);
    }

    public boolean deleteInvalidation(String key) {
        return Boolean.TRUE.equals(redisInvalidationTemplate.delete(key));
    }

    public boolean hasKeyInvalidation(String key) {
        return Boolean.TRUE.equals(redisInvalidationTemplate.hasKey(key));
    }
}