package com.open.hicache.core;

import cn.hutool.core.util.ObjectUtil;
import com.open.hicache.entity.CacheChannelTopic;
import com.open.hicache.utils.SpringContextUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * hicache 核心通知助手
 *
 * @author cuiwy
 * @date 2022/6/4
 */
public class HiCacheHelper {

    /**
     * 通知配置的API更新状态
     */
    public static void modify() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        getRedisTemplate().convertAndSend(CacheChannelTopic.HI_CACHE_CHANNEL_TOPIC, request.getRequestURI());
    }

    private static RedisTemplate redisTemplate;

    private static RedisTemplate getRedisTemplate() {
        if (ObjectUtil.isNull(redisTemplate)) {
            redisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        }
        return redisTemplate;
    }

}
