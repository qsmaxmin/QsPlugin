package com.qsmaxmin.plugin.model;

import com.squareup.javapoet.ClassName;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/10/25 11:56
 * @Description
 */
public class QualifiedItem {
    private String    qualifiedName;
    private ClassName className;

    public QualifiedItem(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        this.className = ClassName.bestGuess(qualifiedName);
    }

    public ClassName getClassName() {
        return className;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public ClassName getViewBindExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + "_QsBind");
    }

    public ClassName getPropertyExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + "_QsConfig");
    }

    public ClassName getEventExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + "_QsEvent");
    }
}
