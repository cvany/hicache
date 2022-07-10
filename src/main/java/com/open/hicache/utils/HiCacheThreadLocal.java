package com.open.hicache.utils;

/**
 * @author cuiwy
 * @date 2022/6/19
 */
public class HiCacheThreadLocal {

    /**
     * for cache status
     */
    private static ThreadLocal<Boolean> status = new InheritableThreadLocal<>();
    /**
     * for cache value
     */
    private static ThreadLocal<Object> cache = new InheritableThreadLocal<>();

    /**
     * set status
     *
     * @param param
     */
    public static void setStatus(Boolean param) {
        status.set(param);
    }

    /**
     * get status
     *
     * @return
     */
    public static Boolean getStatus() {
        return status.get();
    }

    /**
     * set value
     *
     * @param object
     * @param <T>
     */
    public static <T> void setCacheVal(T object) {
        cache.set(object);
    }

    /**
     * get value
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCacheVal() {
        return (T) cache.get();
    }

    /**
     * clear value
     */
    public static void clean() {
        cache.remove();
        status.remove();
    }
}
