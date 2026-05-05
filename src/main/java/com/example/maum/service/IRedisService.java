package com.example.maum.service;

public interface IRedisService {

    void setValues(String key, String data, long duration);

    String getValues(String key);

    void deleteValues(String key);

    boolean hasKey(String key);
}
