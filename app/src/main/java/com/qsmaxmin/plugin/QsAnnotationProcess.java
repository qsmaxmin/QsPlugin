package com.qsmaxmin.plugin;

import com.google.auto.service.AutoService;
import com.qsmaxmin.plugin.executor.EventProcess;
import com.qsmaxmin.plugin.executor.PropertyProcess;
import com.qsmaxmin.plugin.model.QualifiedItem;
import com.qsmaxmin.plugin.executor.ViewBindProcess;
import com.qsmaxmin.qsbase.common.config.Property;
import com.qsmaxmin.qsbase.common.event.Subscribe;
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
        printMessage("started........................ annotations size:" + annotations.size());

        ViewBindProcess viewBindProcess = new ViewBindProcess(this);
        List<QualifiedItem> viewBindItemList = viewBindProcess.process(roundEnv);
        printMessage("\tViewBindProcess complete, process file size:" + viewBindItemList.size());

        PropertyProcess propertyProcess = new PropertyProcess(this);
        List<QualifiedItem> propertyItemList = propertyProcess.process(roundEnv);
        printMessage("\tPropertyProcess complete, process file size:" + propertyItemList.size());

        EventProcess eventProcess = new EventProcess(this);
        List<QualifiedItem> processItemList = eventProcess.process(roundEnv);
        printMessage("\tEventProcess complete, process file size:" + processItemList.size());

        generateAptLinkFile();

        long endTime = System.currentTimeMillis();
        printMessage("end.........................annotation process complete, use time:" + (endTime - startTime) + "ms");
        return true;
    }

    private void generateAptLinkFile() {
        //generate class
        ClassName targetClassName = ClassName.bestGuess("com.qsmaxmin.ann.viewbind.AnnotationExecutorFinder");
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(targetClassName.simpleName()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        //view bind method
        MethodSpec.Builder viewBindLinkMethod = generateViewBindLinkMethod();
        classBuilder.addMethod(viewBindLinkMethod.build());

        //property method
        MethodSpec.Builder propertyLinkMethod = generatePropertyLinkMethod();
        classBuilder.addMethod(propertyLinkMethod.build());

        //event bus method
        MethodSpec.Builder eventLinkMethod = generateEventLinkMethod();
        classBuilder.addMethod(eventLinkMethod.build());

        //common private method
        MethodSpec.Builder commonExecutorMethod = generateCommonExecutorMethod();
        classBuilder.addMethod(commonExecutorMethod.build());

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


    public ProcessingEnvironment getProcessingEnv() {
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
        hashSet.add(Subscribe.class.getCanonicalName());
        return hashSet;
    }

    public void printMessage(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + message);
    }

    /**
     * 生成源码和apt的view层的桥接函数
     */
    private MethodSpec.Builder generateViewBindLinkMethod() {
        MethodSpec.Builder viewMethodBuilder = MethodSpec.methodBuilder("getViewAnnotationExecutor").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        viewMethodBuilder.addParameter(Class.class, "clazz");
        viewMethodBuilder.returns(Object.class);
        viewMethodBuilder.addCode(" return getExecutor(clazz, $S, true);\n", QualifiedItem.getViewBindExtraName());
        return viewMethodBuilder;
    }

    /**
     * 生成源码和apt的Properties桥接方法
     */
    private MethodSpec.Builder generatePropertyLinkMethod() {
        MethodSpec.Builder viewMethodBuilder = MethodSpec.methodBuilder("getPropertiesExecutor").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        viewMethodBuilder.addParameter(Class.class, "clazz");
        viewMethodBuilder.returns(Object.class);
        viewMethodBuilder.addCode(" return getExecutor(clazz, $S, false);\n", QualifiedItem.getPropertyExtraName());
        return viewMethodBuilder;
    }

    private MethodSpec.Builder generateEventLinkMethod() {
        MethodSpec.Builder viewMethodBuilder = MethodSpec.methodBuilder("getEventExecutor").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        viewMethodBuilder.addParameter(Class.class, "clazz");
        viewMethodBuilder.returns(Object.class);
        viewMethodBuilder.addCode(" return getExecutor(clazz, $S, false);\n", QualifiedItem.getEventExtraName());
        return viewMethodBuilder;
    }

    private MethodSpec.Builder generateCommonExecutorMethod() {
        MethodSpec.Builder viewMethodBuilder = MethodSpec.methodBuilder("getExecutor").addModifiers(Modifier.PRIVATE, Modifier.STATIC);
        viewMethodBuilder.addParameter(Class.class, "clazz");
        viewMethodBuilder.addParameter(String.class, "extraName");
        viewMethodBuilder.addParameter(boolean.class, "supportInnerClass");
        viewMethodBuilder.returns(Object.class);

        viewMethodBuilder.addCode("String className;\n" +
                "String name = clazz.getName();\n" +
                "int index_ = name.indexOf('$$');\n" +
                "if (index_ != -1) {\n" +
                "    if (supportInnerClass) {\n" +
                "        int pointIndex = name.lastIndexOf('.');\n" +
                "        String packageName = name.substring(0, pointIndex);\n" +
                "        String simpleName = name.substring(index_ + 1);\n" +
                "        className = packageName + \".\" + simpleName + extraName;\n" +
                "    } else {\n" +
                "        return null;\n" +
                "    }\n" +
                "} else {\n" +
                "    className = name + extraName;\n" +
                "}\n" +
                "try {\n" +
                "    Class<?> myClass = Class.forName(className);\n" +
                "    return myClass.newInstance();\n" +
                "} catch (Exception e) {\n" +
                "    e.printStackTrace();\n" +
                "    return null;\n" +
                "}\n");
        return viewMethodBuilder;
    }
}
