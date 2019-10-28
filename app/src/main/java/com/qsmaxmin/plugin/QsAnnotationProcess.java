package com.qsmaxmin.plugin;

import com.google.auto.service.AutoService;
import com.qsmaxmin.qsbase.common.config.Property;
import com.qsmaxmin.qsbase.common.viewbind.annotation.Bind;
import com.qsmaxmin.qsbase.common.viewbind.annotation.BindBundle;
import com.qsmaxmin.qsbase.common.viewbind.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/6/6 11:13
 * @Description
 */
@AutoService(Processor.class)
public class QsAnnotationProcess extends AbstractProcessor {
    private static final String TAG = "QsAnnotationProcess:";

    @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return true;
        long startTime = System.currentTimeMillis();
        printMessage("start.............. annotations total size:" + annotations.size());

        ViewBindProcess viewBindProcess = new ViewBindProcess(this);
        List<QualifiedItem> viewBindNameList = viewBindProcess.process(roundEnv);

        PropertyProcess propertyProcess = new PropertyProcess(this);
        List<QualifiedItem> propertyNameList = propertyProcess.process(roundEnv);


        if (isListNotEmpty(viewBindNameList) || isListNotEmpty(propertyNameList)) {
            generateAptLinkFile(viewBindNameList, propertyNameList);
        }

        long endTime = System.currentTimeMillis();
        printMessage("end.................annotation process complete, use time:" + (endTime - startTime) + "ms");
        return true;
    }

    private void generateAptLinkFile(List<QualifiedItem> viewBindQualifiedItems, List<QualifiedItem> propertyQualifiedItems) {
        //generate class
        ClassName targetClassName = ClassName.bestGuess("com.qsmaxmin.ann.viewbind.AnnotationExecutorFinder");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(targetClassName.simpleName()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        //view bind method
        MethodSpec.Builder viewBindLinkMethod = generateViewBindLinkMethod(viewBindQualifiedItems);
        if (viewBindLinkMethod != null) {
            classBuilder.addMethod(viewBindLinkMethod.build());
        }

        //property method
        MethodSpec.Builder propertyLinkMethod = generatePropertyLinkMethod(propertyQualifiedItems);
        if (propertyLinkMethod != null) {
            classBuilder.addMethod(propertyLinkMethod.build());
        }

        //generate file
        printMessage("generateClass.......class:" + targetClassName);
        try {
            JavaFile javaFile = JavaFile.builder(targetClassName.packageName(), classBuilder.build()).build();
            javaFile.writeTo(getProcessingEnv().getFiler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isListNotEmpty(List list) {
        return list != null && !list.isEmpty();
    }


    ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Bind.class.getCanonicalName());
        hashSet.add(BindBundle.class.getCanonicalName());
        hashSet.add(OnClick.class.getCanonicalName());
        hashSet.add(Property.class.getCanonicalName());
        return hashSet;
    }

    void printMessage(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + message);
    }

    /**
     * 生成源码和apt的view层的桥接函数
     */
    @Nullable private MethodSpec.Builder generateViewBindLinkMethod(List<QualifiedItem> itemList) {
        return generateLinkMethodByName(itemList, "getViewAnnotationExecutor");
    }

    /**
     * 生成源码和apt的Properties桥接方法
     */
    @Nullable private MethodSpec.Builder generatePropertyLinkMethod(List<QualifiedItem> itemList) {
        return generateLinkMethodByName(itemList, "getPropertiesExecutor");
    }

    @Nullable private MethodSpec.Builder generateLinkMethodByName(List<QualifiedItem> itemList, String methodName) {
        if (itemList == null || itemList.isEmpty()) return null;
        MethodSpec.Builder viewMethodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        viewMethodBuilder.addParameter(String.class, "clazzName");
        viewMethodBuilder.returns(Object.class);
        viewMethodBuilder.addCode("switch (clazzName){\n");
        for (QualifiedItem item : itemList) {
            viewMethodBuilder.addCode("\tcase $S:\n", item.getNameWith$String());
            viewMethodBuilder.addCode("\t\treturn new $T();\n", item.getExecuteClassName());
        }
        viewMethodBuilder.addCode("}\nreturn null;\n");
        return viewMethodBuilder;
    }
}
