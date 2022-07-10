package com.open.hicache.entity;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * api 状态维护
 *
 * @author cuiwy
 * @date 2022/6/5
 */
@Data
public class ApiStatusMaintain {

    /**
     * api 状态维护集合
     * key：接口URI
     * value：eTag值
     */
    private Map<String, String> api = Maps.newConcurrentMap();
}
