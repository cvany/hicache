package com.open.hicache.utils;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.context.ApplicationContext;

/**
 * spring 上下文环境
 *
 * @author cuiwy
 * @date 2022/6/4
 */
public class SpringContextUtil {

    private static ApplicationContext APPLICATION_CONTEXT;

    /**
     * 获取bean
     *
     * @param obj
     * @param <T>
     */
    public static <T> T getBean(Class<T> obj) {
        return getApplicationContext().getBean(obj);
    }

    /**
     * 设置spring 上下文环境
     *
     * @param applicationContext
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (ObjectUtil.isNull(APPLICATION_CONTEXT)) {
            APPLICATION_CONTEXT = applicationContext;
        }
    }

    /**
     * 获取spring上下文环境
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return APPLICATION_CONTEXT;
    }


}
