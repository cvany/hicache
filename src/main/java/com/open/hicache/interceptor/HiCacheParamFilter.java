package com.open.hicache.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.open.hicache.core.RedisContainerContext;
import com.open.hicache.entity.ApiCacheTimeConfig;
import com.open.hicache.entity.HiCacheConfig;
import com.open.hicache.utils.HiCacheThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

/**
 * 参数化级别过滤器
 *
 * @author cuiwy
 * @date 2022/6/18
 */
@Slf4j
public class HiCacheParamFilter extends OncePerRequestFilter {
    @Autowired
    ApiCacheTimeConfig timeConfig;
    @Autowired
    HiCacheConfig hiCacheConfig;
    @Lazy
    @Autowired
    RedisContainerContext redisContainerContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //无配置 缓存接口时
            if (CollUtil.isEmpty(timeConfig.getApi())) {
                filterChain.doFilter(request, response);
                return;
            }
            //1.判断该接口支持缓存
            ApiCacheTimeConfig.InnerBean configBean = timeConfig.getApi().get(request.getRequestURI());
            if (ObjectUtil.isNull(configBean)) {
                filterChain.doFilter(request, response);
                return;
            }

            //该URI未开启参数化缓存
            if (!configBean.isOn()) {
                filterChain.doFilter(request, response);
                return;
            }

            //2.根据参数生成 etag，从redis获取该key 是否存在

            String eTagHeaderValue;
            if (HttpMethod.GET.name().equals(request.getMethod())) {
                eTagHeaderValue = this.generateETagHeaderValue(request.getInputStream(), request.getQueryString().getBytes());
            } else {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
//                String body = IoUtil.read(reader);
                eTagHeaderValue = this.generateETagHeaderValue(request.getInputStream(), null);
            }

            ValueOperations valueOperations = redisContainerContext.getRedisTemplate().opsForValue();
            Object val = valueOperations.get(eTagHeaderValue);
            if (ObjectUtil.isNotNull(val)) {
                //缓存生效
                response.setStatus(NOT_MODIFIED.value());
                response.setHeader(ETAG, request.getHeader(HttpHeaders.IF_NONE_MATCH));
            } else {
                //invoke method after,will use it
                HiCacheThreadLocal.setStatus(Boolean.TRUE);
                HiCacheThreadLocal.setCacheVal(eTagHeaderValue);
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            log.error("HiCacheParamFilter error:{}", e);
            filterChain.doFilter(request, response);
        }

    }

    private String generateETagHeaderValue(InputStream inputStream, byte[] bytes) throws IOException {
        StringBuilder builder = new StringBuilder(32);
        if (ObjectUtil.isNotNull(bytes)) {
            DigestUtils.appendMd5DigestAsHex(bytes, builder);
        } else {
            DigestUtils.appendMd5DigestAsHex(inputStream, builder);
        }
        return builder.toString();
    }
}
