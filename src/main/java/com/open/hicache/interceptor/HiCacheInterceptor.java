package com.open.hicache.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.open.hicache.annotation.HiCache;
import com.open.hicache.core.HiCacheHelper;
import com.open.hicache.core.RedisContainerContext;
import com.open.hicache.entity.ApiCacheTimeConfig;
import com.open.hicache.entity.ApiStatusMaintain;
import com.open.hicache.entity.HiCacheConfig;
import com.open.hicache.utils.HiCacheThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

import static com.open.hicache.entity.HiCacheConstant.HI_CACHE;
import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

/**
 * 缓存拦截器
 *
 * @author cuiwy
 * @date 2022/6/3
 */
@Slf4j
public class HiCacheInterceptor implements HandlerInterceptor {
    @Autowired
    HiCacheConfig hiCacheConfig;
    @Autowired
    ApiCacheTimeConfig timeConfig;
    @Autowired
    ApiStatusMaintain apiStatusMaintain;
    @Lazy
    @Autowired
    RedisContainerContext redisContainerContext;

    private LastModifiedHandler lastModifiedHandler;

    public HiCacheInterceptor(LastModifiedHandler lastModifiedHandler) {
        this.lastModifiedHandler = lastModifiedHandler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            //总开关
            if (hiCacheConfig.isOff()) {
                return true;
            }
            //无配置 缓存接口时
            if (CollUtil.isEmpty(timeConfig.getApi())) {
                return true;
            }

            //优先校验 eTag
            Boolean res = this.validateEtag(request, response);
            if (ObjectUtil.isNotNull(res)) {
                return res;
            }

            //次校验 配制接口缓存时间
            ApiCacheTimeConfig.InnerBean configBean = timeConfig.getApi().get(request.getRequestURI());
            if (ObjectUtil.isNull(configBean)) {
                return true;
            }

            //if config zero ,user default cache time
            long lastModifiedTimestamp = configBean.getVal().equals(0L) ? lastModifiedHandler.getLastModifiedInternal() : lastModifiedHandler.getLastModifiedInternal(configBean.getVal());
            boolean modified = new ServletWebRequest(request, response).checkNotModified(lastModifiedTimestamp);
            return !modified;
        } catch (Exception e) {
            log.error("HiCacheInterceptor error:{}", e);
        }
        return false;
    }

    private Boolean validateEtag(HttpServletRequest request, HttpServletResponse response) {
        String eTag = request.getHeader(IF_NONE_MATCH);
        if (StrUtil.isNotBlank(eTag)) {
            String modifiedEtag = apiStatusMaintain.getApi().get(request.getRequestURI());
            //eTag already update
            if (StrUtil.isNotBlank(modifiedEtag)) {
                if (!modifiedEtag.equals(eTag)) {
                    //set etag
                    response.setHeader(ETAG, modifiedEtag);
                    return true;
                }

                //return 304
                response.setStatus(NOT_MODIFIED.value());
                response.setHeader(ETAG, modifiedEtag);
                return false;

            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //执行handler 发生了错误 不进行通知
        if (ObjectUtil.isNotNull(ex)) {
            return;
        }
        //注解通知--通知处理
        HiCache annotation = ((HandlerMethod) handler).getMethod().getDeclaredAnnotation(HiCache.class);
        if (ObjectUtil.isNotNull(annotation)) {
            HiCacheHelper.modify();
        }

        //缓存处理
        ApiCacheTimeConfig.InnerBean configBean = timeConfig.getApi().get(request.getRequestURI());
        if (ObjectUtil.isNull(configBean)) {
            return;
        }
        //根据过滤器中处理结果
        try {
            Boolean status = HiCacheThreadLocal.getStatus();
            if (ObjectUtil.isNotNull(status) && status) {
                ValueOperations valueOperations = redisContainerContext.getRedisTemplate().opsForValue();
                valueOperations.set(HI_CACHE + HiCacheThreadLocal.getCacheVal(), StrUtil.DASHED, configBean.getVal(), TimeUnit.MILLISECONDS);
            }
        } finally {
            //release memory
            HiCacheThreadLocal.clean();
        }
    }
}
