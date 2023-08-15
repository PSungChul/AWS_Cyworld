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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////RefreshToken - 리프레쉬
    public void set(String key, Object o, long millis) {
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisTemplate.opsForValue().set(key, o, millis, TimeUnit.MILLISECONDS);
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////BlackList - 무효화 및 로그아웃
    public void setBlackList(String key, Object o, long millis) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, millis, TimeUnit.MILLISECONDS);
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
}