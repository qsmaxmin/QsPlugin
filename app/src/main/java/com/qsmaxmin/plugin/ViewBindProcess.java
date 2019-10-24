package com.qsmaxmin.plugin;

import com.qsmaxmin.qsbase.common.viewbind.annotation.Bind;
import com.qsmaxmin.qsbase.common.viewbind.annotation.BindBundle;
import com.qsmaxmin.qsbase.common.viewbind.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/7/22 17:22
 * @Description
 */
class ViewBindProcess {
    private final QsAnnotationProcess mProcess;

    ViewBindProcess(QsAnnotationProcess process) {
        this.mProcess = process;
    }

    void process(RoundEnvironment roundEnv) {
        ClassName superClassName = ClassName.bestGuess("com.qsmaxmin.qsbase.common.viewbind.AnnotationExecutor");
        List<String> qualifiedNameList = new ArrayList<>();

        HashMap<String, List<String>> findIdCodeHolder = new HashMap<>();
        HashMap<String, List<String>> setFieldCodeHolder = new HashMap<>();

        Set<? extends Element> bindViewElement = roundEnv.getElementsAnnotatedWith(Bind.class);
        if (bindViewElement != null) mProcess.printMessage("...@Bind element size:" + bindViewElement.size());

        HashMap<String, Map<String, List<String>>> setListenerCodeHolder = new HashMap<>();
        Set<? extends Element> onClickElement = roundEnv.getElementsAnnotatedWith(OnClick.class);
        if (onClickElement != null) mProcess.printMessage("...@OnClick element size:" + onClickElement.size());

        HashMap<String, List<String>> bindBundleCodeHolder = new HashMap<>();
        Set<? extends Element> bindBundleElement = roundEnv.getElementsAnnotatedWith(BindBundle.class);
        if (bindBundleElement != null) mProcess.printMessage("...@BindBundle element size:" + bindBundleElement.size());


        //------------bind view logic-------------
        if (bindViewElement != null) {
            for (Element element : bindViewElement) {
                if (element.getKind() != ElementKind.FIELD) continue;
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                String qualifiedName = enclosingElement.getQualifiedName().toString();
                if (!qualifiedNameList.contains(qualifiedName)) qualifiedNameList.add(qualifiedName);

                Bind bind = element.getAnnotation(Bind.class);
                int viewId = bind.value();

                List<String> findIdCodeList = findIdCodeHolder.get(qualifiedName);
                if (findIdCodeList == null) {
                    findIdCodeList = new ArrayList<>();
                    findIdCodeHolder.put(qualifiedName, findIdCodeList);
                }
                List<String> setFieldCodeList = setFieldCodeHolder.get(qualifiedName);
                if (setFieldCodeList == null) {
                    setFieldCodeList = new ArrayList<>();
                    setFieldCodeHolder.put(qualifiedName, setFieldCodeList);
                }

                String findIdCode = "View v_" + viewId + " = view.findViewById(" + viewId + ");\n";
                String setFieldCode = "if (v_" + viewId + " != null) target." + element.getSimpleName().toString() + " = forceCastView(v_" + viewId + ");\n";
                if (!findIdCodeList.contains(findIdCode)) findIdCodeList.add(findIdCode);
                setFieldCodeList.add(setFieldCode);
            }
        }


        //------------onClick view logic-------------
        if (onClickElement != null) {
            for (Element element : onClickElement) {
                if (element.getKind() != ElementKind.METHOD) continue;
                ExecutableElement executableElement = (ExecutableElement) element;
                String methodName = executableElement.getSimpleName().toString();

                OnClick onClick = element.getAnnotation(OnClick.class);
                int[] onClickIds = onClick.value();
                if (onClickIds.length == 0) continue;
                ArrayList<Integer> tempList = new ArrayList<>();
                for (int id : onClickIds) {
                    if (!tempList.contains(id)) {
                        tempList.add(id);
                    }
                }

                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                String qualifiedName = enclosingElement.getQualifiedName().toString();
                if (!qualifiedNameList.contains(qualifiedName)) qualifiedNameList.add(qualifiedName);

                Map<String, List<String>> stringListMap = setListenerCodeHolder.get(qualifiedName);
                if (stringListMap == null) {
                    stringListMap = new HashMap<>();
                    setListenerCodeHolder.put(qualifiedName, stringListMap);
                }

                List<String> setListenerCodeList = stringListMap.get(methodName);
                if (setListenerCodeList == null) {
                    setListenerCodeList = new ArrayList<>();
                    stringListMap.put(methodName, setListenerCodeList);
                }

                for (int onClickId : tempList) {
                    List<String> findIdCodeList = findIdCodeHolder.get(qualifiedName);
                    if (findIdCodeList == null) {
                        findIdCodeList = new ArrayList<>();
                        findIdCodeHolder.put(qualifiedName, findIdCodeList);
                    }
                    String findIdCode = "View v_" + onClickId + " = view.findViewById(" + onClickId + ");\n";
                    if (!findIdCodeList.contains(findIdCode)) findIdCodeList.add(findIdCode);

                    String setListenerCode = "if (v_" + onClickId + " != null) v_" + onClickId + ".setOnClickListener(" + methodName + "Listener);\n";
                    setListenerCodeList.add(setListenerCode);
                }
            }
        }

        //------------bind bundle logic-------------
        if (bindBundleElement != null) {
            for (Element element : bindBundleElement) {
                if (element.getKind() == ElementKind.FIELD) {
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    String qualifiedName = enclosingElement.getQualifiedName().toString();
                    if (!qualifiedNameList.contains(qualifiedName)) qualifiedNameList.add(qualifiedName);

                    BindBundle bind = element.getAnnotation(BindBundle.class);
                    List<String> keyList = bindBundleCodeHolder.get(qualifiedName);
                    if (keyList == null) {
                        keyList = new ArrayList<>();
                        bindBundleCodeHolder.put(qualifiedName, keyList);
                    }

                    String bundleKey = bind.value();
                    Name simpleName = element.getSimpleName();
                    String code0 = "Object bv_" + simpleName + " = bundle.get(\"" + bundleKey + "\");\n";
                    String code1 = "if(bv_" + simpleName + " != null)target." + simpleName + " = forceCastObject(bv_" + simpleName + ");" + "\n";
                    keyList.add(code0 + code1);
                }
            }
        }

        //--------------- create class file logic------------------
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = dateFormat.format(System.currentTimeMillis());
        String docStr = "该类由QsPlugin插件自动生成 " + timeStr +
                "\nQsPlugin插件需要配合QsBase框架一起使用，使用时请确保该插件版本和QsBase框架版本号一致，否则可能会出现不可预期的错误！\n";

        for (String qualifiedName : qualifiedNameList) {
            ClassName className = ClassName.bestGuess(qualifiedName);
            TypeSpec.Builder typeSpecBuilder = generateClass(className, superClassName, docStr);

            List<String> findIdCodeList = findIdCodeHolder.get(qualifiedName);
            if (findIdCodeList != null && !findIdCodeList.isEmpty()) {
                MethodSpec.Builder bindViewBuilder = createBindViewMethod(className);
                bindViewBuilder.addCode("//find views id base on @Bind and @OnClick annotation\n");
                for (String code : findIdCodeList) {
                    bindViewBuilder.addCode(code);
                }

                List<String> setFieldCodeList = setFieldCodeHolder.get(qualifiedName);
                if (setFieldCodeList != null && !setFieldCodeList.isEmpty()) {
                    bindViewBuilder.addCode("\n//set field base on @Bind annotation\n");
                    for (String code : setFieldCodeList) {
                        bindViewBuilder.addCode(code);
                    }
                }

                Map<String, List<String>> stringListMap = setListenerCodeHolder.get(qualifiedName);
                if (stringListMap != null && !stringListMap.isEmpty()) {
                    for (String methodName : stringListMap.keySet()) {
                        bindViewBuilder.addCode("\n//set click listener base on @OnClick annotation, method name:" + methodName + "\n");
                        String listenerCode = "View.OnClickListener " + methodName + "Listener = new View.OnClickListener() {\n" +
                                "   @Override public void onClick(View v) {\n" +
                                "       target." + methodName + "(v);\n" +
                                "   }\n" +
                                "};\n";
                        bindViewBuilder.addCode(listenerCode);
                        List<String> onClickCodeList = stringListMap.get(methodName);
                        for (String code : onClickCodeList) {
                            bindViewBuilder.addCode(code);
                        }
                    }
                }
                typeSpecBuilder.addMethod(bindViewBuilder.build());
            }


            List<String> bindBundleCodeList = bindBundleCodeHolder.get(qualifiedName);
            if (bindBundleCodeList != null && !bindBundleCodeList.isEmpty()) {
                MethodSpec.Builder bindBundleBuilder = createBindBundleMethod(className);
                for (String code : bindBundleCodeList) {
                    bindBundleBuilder.addCode(code);
                }
                typeSpecBuilder.addMethod(bindBundleBuilder.build());
            }

            try {
                JavaFile javaFile = JavaFile.builder(className.packageName(), typeSpecBuilder.build()).build();
                javaFile.writeTo(mProcess.getProcessingEnv().getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }

            generateInitClass(qualifiedNameList);
        }
    }

    private TypeSpec.Builder generateClass(ClassName className, ClassName superClassName, String docStr) {
        String simpleName = className.simpleName();
        String extraName = "_QsAnn";
        String realClassName = simpleName + extraName;
        TypeSpec.Builder builder = TypeSpec.classBuilder(realClassName).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        ClassName enclosingClassName = className.enclosingClassName();
        ClassName typeName;
        if (enclosingClassName != null) {
            mProcess.printMessage("generateClass.......fit inner class:" + className + extraName);
            typeName = ClassName.bestGuess(className.packageName() + "." + enclosingClassName.simpleName() + "." + simpleName);
        } else {
            mProcess.printMessage("generateClass.......class:" + className + extraName);
            typeName = ClassName.bestGuess(className.packageName() + "." + simpleName);
        }
        builder.superclass(ParameterizedTypeName.get(superClassName, typeName));
        builder.addJavadoc(docStr);
        return builder;
    }

    private MethodSpec.Builder createBindViewMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindView");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target", Modifier.FINAL)
                .addParameter(ClassName.bestGuess("android.view.View"), "view");
        builder.addJavadoc("Find the corresponding control object according to the ID of the @Bind and @OnClick annotations and bind it to the current View layer\n");
        return builder;
    }

    private MethodSpec.Builder createBindBundleMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindBundle");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target")
                .addParameter(ClassName.bestGuess("android.os.Bundle"), "bundle");
        builder.addJavadoc("Find the corresponding Bundle passed value from the @BindBundle annotation string and bind it to the current View layer\n");
        return builder;
    }


    private void generateInitClass(List<String> qualifiedNameList) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("ViewBindInitImpl").addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ClassName superClassName = ClassName.bestGuess("com.qsmaxmin.qsbase.common.viewbind.ViewBindInit");
        classBuilder.addSuperinterface(superClassName);

        ClassName executorName = ClassName.bestGuess("com.qsmaxmin.qsbase.common.viewbind.AnnotationExecutor");
        ClassName clazzName = ClassName.get(Class.class);
        ClassName hashMapName = ClassName.get(HashMap.class);


        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("init").addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ParameterizedTypeName typeName = ParameterizedTypeName.get(hashMapName, clazzName, executorName);
        methodBuilder.addParameter(typeName, "map");
        for (String qualifiedName : qualifiedNameList) {
            methodBuilder.addCode("map.put(" + qualifiedName + ".class, new " + getExecuteClassName(qualifiedName) + "());\n");
        }
        classBuilder.addMethod(methodBuilder.build());

        try {
            JavaFile javaFile = JavaFile.builder(superClassName.packageName(), classBuilder.build()).build();
            javaFile.writeTo(mProcess.getProcessingEnv().getFiler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getExecuteClassName(String qualifiedName) {
        ClassName className = ClassName.bestGuess(qualifiedName);
        ClassName enclosingClassName = className.enclosingClassName();
        if (enclosingClassName != null) {
            return enclosingClassName.packageName() + "." + className.simpleName() + "_QsAnn";
        } else {
            return qualifiedName + "_QsAnn";
        }
    }
}
