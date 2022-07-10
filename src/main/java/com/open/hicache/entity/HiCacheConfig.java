package com.open.hicache.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * hicache 全局配置
 *
 * @author cuiwy
 * @date 2022/6/11
 */
@Data
@ConfigurationProperties("hi.cache")
public class HiCacheConfig {

    /**
     * 全局总开关
     */
    private boolean off;
}
