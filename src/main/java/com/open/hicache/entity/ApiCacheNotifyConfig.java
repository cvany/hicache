package com.open.hicache.entity;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * api 通知配置实体
 *
 * @author cuiwy
 * @date 2022/6/4
 */
@Data
@ConfigurationProperties("hi.cache.notify")
public class ApiCacheNotifyConfig {

    /**
     * 配置的当前API刷新了状态，需对应通知配置缓存的接口立即放行
     * key：代表 当前API
     * value：代表 需通知的API（支持配置多个，用英文逗号隔开）
     */
    private Map<String, String> api = Maps.newConcurrentMap();
}
