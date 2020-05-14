package com.qsmaxmin.qsbase.common.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @CreateBy administrator
 * @Date 2020/5/14 12:47
 * @Description 编译时有该注解就能生成APT代码
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface QsAnn {
    /**
     * 当前项目是否是library
     */
    boolean isLibrary() default false;
}
