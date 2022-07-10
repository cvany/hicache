package com.open.hicache.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.open.hicache.entity.ApiCacheNotifyConfig;
import com.open.hicache.entity.ApiStatusMaintain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 消息监听器
 *
 * @author cuiwy
 * @date 2022/6/4
 */
@Slf4j
public class CacheRedisMessageListenerAdapter extends MessageListenerAdapter {
    @Autowired
    ApiCacheNotifyConfig notifyConfig;
    @Autowired
    ApiStatusMaintain apiStatusMaintain;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("message:{}", message.toString());
        if (CollUtil.isEmpty(notifyConfig.getApi())) {
            return;
        }
        String notifyApi = notifyConfig.getApi().get(message.toString());
        if (StrUtil.isBlank(notifyApi)) {
            return;
        }
        String[] arr = notifyApi.split(",");
        for (String api : arr) {
            apiStatusMaintain.getApi().put(api, String.valueOf(System.nanoTime()));
        }
    }
}
