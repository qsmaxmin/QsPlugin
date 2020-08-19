package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.annotation.bind.Bind;
import com.qsmaxmin.annotation.bind.BindBundle;
import com.qsmaxmin.annotation.bind.OnClick;
import com.qsmaxmin.plugin.QsAnnotationProcess;
import com.qsmaxmin.plugin.model.ViewBindData;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/7/22 17:22
 * @Description
 */
public class ViewBindProcess extends BaseProcess {

    public ViewBindProcess(QsAnnotationProcess process) {
        super(process);
    }

    private int getSize(Collection<? extends Element> collection) {
        return collection == null ? 0 : collection.size();
    }

    private ViewBindData queryData(HashMap<String, ViewBindData> map, String qualifiedName) {
        ViewBindData data = map.get(qualifiedName);
        if (data == null) {
            data = new ViewBindData(qualifiedName);
            map.put(qualifiedName, data);
        }
        return data;
    }

    private String getQualifiedName(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        return enclosingElement.getQualifiedName().toString();
    }

    @Override public int process(RoundEnvironment roundEnv) {
        Set<? extends Element> bindViewElement = roundEnv.getElementsAnnotatedWith(Bind.class);
        Set<? extends Element> onClickElement = roundEnv.getElementsAnnotatedWith(OnClick.class);
        Set<? extends Element> bindBundleElement = roundEnv.getElementsAnnotatedWith(BindBundle.class);
        int bindViewSize = getSize(bindViewElement);
        int onClickSize = getSize(onClickElement);
        int bindBundleSize = getSize(bindBundleElement);
        if (bindViewSize == 0 && onClickSize == 0 && bindBundleSize == 0) {
            return 0;
        }
        printMessage("ViewBindProcess :@Bind element size:" + bindViewSize + ",  @OnClick element size:" + onClickSize + ",  @BindBundle element size:" + bindBundleSize);

        HashMap<String, ViewBindData> map = new HashMap<>();
        //------------bind view logic-------------
        if (bindViewElement != null) {
            for (Element element : bindViewElement) {
                if (element.getKind() != ElementKind.FIELD) continue;
                String qualifiedName = getQualifiedName(element);
                ViewBindData data = queryData(map, qualifiedName);
                data.addBindData(element, element.getAnnotation(Bind.class));
            }
        }

        if (onClickElement != null) {
            for (Element element : onClickElement) {
                if (element.getKind() != ElementKind.METHOD) continue;
                ExecutableElement executableElement = (ExecutableElement) element;

                String qualifiedName = getQualifiedName(element);
                OnClick onClick = element.getAnnotation(OnClick.class);
                ViewBindData data = queryData(map, qualifiedName);
                data.addOnClickData(executableElement, onClick);
            }
        }

        if (bindBundleElement != null) {
            for (Element element : bindBundleElement) {
                if (element.getKind() == ElementKind.FIELD) {
                    BindBundle bindBundle = element.getAnnotation(BindBundle.class);
                    String qualifiedName = getQualifiedName(element);
                    ViewBindData data = queryData(map, qualifiedName);
                    data.addBindBundleData(element, bindBundle);
                }
            }
        }

        //--------------- create class file logic------------------
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = dateFormat.format(System.currentTimeMillis());
        String docStr = "该类由QsPlugin插件自动生成 " + timeStr + "\nQsPlugin插件需要配合QsBase框架一起使用，使用时请确保该插件版本和QsBase框架版本号一致，否则可能会出现不可预期的错误！\n";

        for (String qualifiedName : map.keySet()) {
            ViewBindData data = map.get(qualifiedName);

            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(data.getViewBindExecuteClassName()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
            typeSpecBuilder.addJavadoc(docStr);

            MethodSpec bindViewMethod = data.generateBindViewMethod();
            if (bindViewMethod != null) {
                typeSpecBuilder.addMethod(bindViewMethod);
            }

            MethodSpec bindBundleMethod = data.generateBindBundleMethod();
            if (bindBundleMethod != null) {
                typeSpecBuilder.addMethod(bindBundleMethod);
            }

            try {
                JavaFile javaFile = JavaFile.builder(data.getClassName().packageName(), typeSpecBuilder.build()).build();
                javaFile.writeTo(getProcess().getProcessingEnv().getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map.size();
    }


}
