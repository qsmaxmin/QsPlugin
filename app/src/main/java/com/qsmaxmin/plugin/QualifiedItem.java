package com.qsmaxmin.plugin;

import com.squareup.javapoet.ClassName;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/10/25 11:56
 * @Description
 */
class QualifiedItem {
    private String    qualifiedName;
    private ClassName className;

    QualifiedItem(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        this.className = ClassName.bestGuess(qualifiedName);
    }

    ClassName getClassName() {
        return className;
    }

    String getQualifiedName() {
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

    ClassName getExecuteClassName() {
        return ClassName.get(className.packageName(), className.simpleName() + "_QsAnn");
    }
}
