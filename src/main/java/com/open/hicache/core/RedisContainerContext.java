package com.open.hicache.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * redis 容器环境
 *
 * @author cuiwy
 * @date 2022/6/4
 */
@Slf4j
public class RedisContainerContext {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Resource
    RedisTemplate redisTemplate;

    /**
     * 发布消息到指定渠道
     *
     * @param channel 渠道
     * @param msg     消息
     */
    public void publish(String channel, Object msg) {
        redisTemplate.convertAndSend(channel, msg);
    }

    /**
     * redis 实例
     *
     * @return
     */
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * redis 连接工厂     *
     *
     * @return
     */
    public RedisConnectionFactory getRedisConnectionFactory() {
        return redisConnectionFactory;
    }

}
