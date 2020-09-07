package com.qsmaxmin.plugin.model;

import com.qsmaxmin.plugin.QsAnnotationProcess;
import com.squareup.javapoet.ClassName;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @CreateBy administrator
 * @Date 2020/9/4 17:53
 * @Description
 */
public class BaseData {
    private final QsAnnotationProcess mProcess;
    private final String              qualifiedName;
    private final ClassName           className;


    public BaseData(QsAnnotationProcess process, String qualifiedName) {
        this.mProcess = process;
        this.qualifiedName = qualifiedName;
        this.className = ClassName.bestGuess(qualifiedName);
    }

    public ClassName getClassName() {
        return className;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    protected boolean isStatic(Element element) {
        Set<Modifier> modifiers = element.getModifiers();
        for (Modifier modifier : modifiers) {
            if (modifier == Modifier.STATIC) {
                return true;
            }
        }
        return false;
    }

    void printMessage(String message) {
        mProcess.printMessage(1, message);
    }
}
