package com.qsmaxmin.qsbase.common.viewbind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 绑定View与layer
 * Fragment或Activity初始化时，支持将layoutId()和loadingLayoutId()指向的布局中的控件绑定到当前Field
 * 由于懒加载机制的原因emptyLayoutId()和errorLayoutId()指向的资源不是立刻加载进来，所以该注解不能绑定这两个布局控件
 * 此时可通过重写onCreateEmptyView()或onCreateErrorView()以达到动态设置的目的
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Bind {
    int value();
}
