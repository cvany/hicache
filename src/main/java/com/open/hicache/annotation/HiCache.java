package com.open.hicache.annotation;

import java.lang.annotation.*;

/**
 * hicache 标识注解
 *
 * @author cuiwy
 * @date 2022/6/5
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HiCache {
    /**
     * hicache 接口名称
     *
     * @return
     */
    String name() default "";
}
