package com.qsmaxmin.plugin;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/6/10 11:28
 * @Description
 */
class MethodSpecHelper {

    static MethodSpec.Builder createBindViewMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindView");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target", Modifier.FINAL)
                .addParameter(ClassName.bestGuess("android.view.View"), "view");
        return builder;
    }


    static MethodSpec.Builder createBindBundleMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindBundle");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target")
                .addParameter(ClassName.bestGuess("android.os.Bundle"), "bundle");
        return builder;
    }
}
