package com.open.hicache.config;

import com.open.hicache.core.CacheRedisMessageListenerAdapter;
import com.open.hicache.interceptor.HiCacheParamFilter;
import com.open.hicache.core.RedisContainerContext;
import com.open.hicache.entity.*;
import com.open.hicache.interceptor.HiCacheInterceptor;
import com.open.hicache.interceptor.LastModifiedHandler;
import com.open.hicache.utils.SpringContextUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * hicache 自动装配
 *
 * @author cuiwy
 * @date 2022/6/3
 */
@Configuration
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnProperty(value = "hi.cache.on", matchIfMissing = true)
@EnableConfigurationProperties({HiCacheConfig.class, ApiCacheTimeConfig.class, ApiCacheNotifyConfig.class})
public class HiCacheAutoConfiguration implements WebMvcConfigurer, InitializingBean {
    @Autowired
    HiCacheInterceptor hiCacheInterceptor;
    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public ApiStatusMaintain apiStatusMaintain() {
        return new ApiStatusMaintain();
    }

    @Bean
    @ConditionalOnMissingBean(HiCacheInterceptor.class)
    public HiCacheInterceptor hiCacheInterceptor() {
        return new HiCacheInterceptor(new LastModifiedHandler());
    }

    @Bean
    public RedisContainerContext redisContainerContext() {
        return new RedisContainerContext();
    }

    @Bean
    public CacheRedisMessageListenerAdapter cacheRedisMessageListenerAdapter() {
        return new CacheRedisMessageListenerAdapter();
    }

    /**
     * 不存在 RedisMessageListenerContainer 则创建
     *
     * @param connectionFactory
     * @param listenerAdapter
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RedisMessageListenerContainer.class)
    @ConditionalOnProperty(value = "hi.cache.property", matchIfMissing = true)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            CacheRedisMessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(CacheChannelTopic.HI_CACHE_CHANNEL_TOPIC));
        return container;
    }

    /**
     * 若存在，则直接添加默认消息监听
     */
    @Configuration
    @ConditionalOnBean(RedisMessageListenerContainer.class)
    public class CacheRedisMessageListenerContainer {
        public CacheRedisMessageListenerContainer() {
            SpringContextUtil.getBean(RedisMessageListenerContainer.class)
                    .addMessageListener(cacheRedisMessageListenerAdapter(),
                            new ChannelTopic(CacheChannelTopic.HI_CACHE_CHANNEL_TOPIC));
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "hi.cache.param.filter", matchIfMissing = true)
    static class CacheParamFilter {

        @Bean
        public HiCacheParamFilter hiCacheParamFilter() {
            return new HiCacheParamFilter();
        }
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hiCacheInterceptor).addPathPatterns("/**");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SpringContextUtil.setApplicationContext(applicationContext);
    }
}
