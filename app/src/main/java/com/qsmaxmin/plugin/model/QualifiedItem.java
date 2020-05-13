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

    String getNameWith$String() {
        ClassName enclose = className.enclosingClassName();
        if (enclose != null) {
            return className.packageName() + "." + enclose.simpleName() + "$" + className.simpleName();
        } else {
            return qualifiedName;
        }
    }

    public static String getViewBindExtraName() {
        return "_QsBind";
    }

    public static String getPropertyExtraName() {
        return "_QsConfig";
    }

    public static String getEventExtraName() {
        return "_QsEvent";
    }

    public ClassName getViewBindExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + getViewBindExtraName());
    }

    public ClassName getPropertyExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + getPropertyExtraName());
    }

    public ClassName getEventExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + getEventExtraName());
    }
}
