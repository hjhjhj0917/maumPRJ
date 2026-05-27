package com.example.maum.service.impl;

import com.example.maum.service.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService implements IRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setValues(String key, String data, long duration) {
        redisTemplate.opsForValue().set(key, data, duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getValues(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void pushMessage(String key, String data) {
        redisTemplate.opsForList().rightPush(key, data);
        redisTemplate.expire(key, 1, TimeUnit.HOURS); // 1시간 동안 유지
    }

    @Override
    public java.util.List<Object> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
