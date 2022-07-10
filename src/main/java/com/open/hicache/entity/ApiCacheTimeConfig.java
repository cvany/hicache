package com.open.hicache.entity;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * API缓存接口配置
 *
 * @author cuiwy
 * @date 2022/6/3
 */
@Data
@ConfigurationProperties("hi.cache.time")
public class ApiCacheTimeConfig {

    /**
     * 配置支持缓存接口的API
     * key：代表 接口URL
     * value：代表 缓存时间（单位；ms）
     */
    private Map<String, InnerBean> api = Maps.newConcurrentMap();


    @Data
    public static class InnerBean {
        /**
         * 参数化缓存开关
         */
        private boolean on;
        /**
         * 缓存时间（单位：ms）
         */
        private Long val;

    }
}
