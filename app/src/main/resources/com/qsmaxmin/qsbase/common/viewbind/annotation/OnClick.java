package com.qsmaxmin.qsbase.common.viewbind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 点击事件注解.
 * <p>
 * Fragment或Activity初始化时，支持将layoutId()和loadingLayoutId()指向的布局中的控件设置点击事件
 * 由于懒加载机制的原因emptyLayoutId()和errorLayoutId()指向的资源不是立刻加载进来，所以该注解不能绑定这两个布局控件
 * 此时可通过重写onCreateEmptyView()或onCreateErrorView()以达到动态设置的目的
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OnClick {
    /**
     * 控件的id集合, 找不到该控件则不绑定点击事件
     */
    int[] value();

    /**
     * 点击间隔时间，默认500ms
     * 间隔小于500ms不生效，防止快速点击
     */
    long clickInterval() default 500L;
}
