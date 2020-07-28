package com.qsmaxmin.qsbase.common.viewbind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 点击事件注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OnClick {
    int[] value();

    long clickInterval() default 300L;
}
