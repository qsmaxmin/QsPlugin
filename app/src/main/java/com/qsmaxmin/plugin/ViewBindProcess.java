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


        HashMap<String, List<String>> bindViewCodeHolder = new HashMap<>();
        HashMap<String, List<BindItem>> bindViewItemHolder = new HashMap<>();
        Set<? extends Element> bindViewElement = roundEnv.getElementsAnnotatedWith(Bind.class);
        if (bindViewElement != null) mProcess.printMessage("...@Bind element size:" + bindViewElement.size());

        HashMap<String, Map<String, List<String>>> onClickHolder = new HashMap<>();
        Set<? extends Element> onClickElement = roundEnv.getElementsAnnotatedWith(OnClick.class);
        if (onClickElement != null) mProcess.printMessage("...@OnClick element size:" + onClickElement.size());

        HashMap<String, List<String>> bindBundleHolder = new HashMap<>();
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
                List<String> codeList = bindViewCodeHolder.get(qualifiedName);
                List<BindItem> itemList = bindViewItemHolder.get(qualifiedName);
                if (codeList == null) {
                    codeList = new ArrayList<>();
                    bindViewCodeHolder.put(qualifiedName, codeList);
                    itemList = new ArrayList<>();
                    bindViewItemHolder.put(qualifiedName, itemList);
                }
                String code = "if(target." + element.getSimpleName().toString() + " == null)" +
                        "target." + element.getSimpleName().toString() + " = view.findViewById(" + viewId + ");\n";
                codeList.add(code);
                BindItem bindItem = new BindItem(element.getSimpleName().toString(), viewId);
                itemList.add(bindItem);
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

                Map<String, List<String>> stringListMap = onClickHolder.get(qualifiedName);
                if (stringListMap == null) {
                    stringListMap = new HashMap<>();
                    onClickHolder.put(qualifiedName, stringListMap);
                }

                List<String> codeList = stringListMap.get(methodName);
                if (codeList == null) {
                    codeList = new ArrayList<>();
                    stringListMap.put(methodName, codeList);
                }

                List<BindItem> bindItems = bindViewItemHolder.get(qualifiedName);
                for (int onClickId : tempList) {
                    BindItem bindItem = null;
                    if (bindItems != null && !bindItems.isEmpty()) {
                        for (BindItem item : bindItems) {
                            if (item.viewId == onClickId) {
                                bindItems.remove(item);
                                bindItem = item;
                                break;
                            }
                        }
                    }

                    if (bindItem != null) {
                        String code = "if(target." + bindItem.fieldName + " != null) target." + bindItem.fieldName + ".setOnClickListener(" + methodName + "Listener);\n";
                        codeList.add(code);
                    } else {
                        String code0 = "View v_" + onClickId + " = view.findViewById(" + onClickId + ");\n";
                        String code1 = "if (v_" + onClickId + " != null) v_" + onClickId + ".setOnClickListener(" + methodName + "Listener);\n";
                        codeList.add(code0 + code1);
                    }
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
                    List<String> keyList = bindBundleHolder.get(qualifiedName);
                    if (keyList == null) {
                        keyList = new ArrayList<>();
                        bindBundleHolder.put(qualifiedName, keyList);
                    }

                    String bundleKey = bind.value();
                    Name simpleName = element.getSimpleName();
                    String typeStr = element.asType().toString();
                    String code0 = "Object bv_" + simpleName + " = bundle.get(\"" + bundleKey + "\");\n";
                    String code1 = "if(bv_" + simpleName + " != null)target." + simpleName + " = (" + typeStr + ")bv_" + simpleName + ";\n ";
                    keyList.add(code0 + code1);
                }
            }
        }

        //--------------- create class file logic------------------
        for (String qualifiedName : qualifiedNameList) {
            ClassName className = ClassName.bestGuess(qualifiedName);

            TypeSpec.Builder typeSpecBuilder = generateClass(className, superClassName);

            MethodSpec.Builder bindViewBuilder = createBindViewMethod(className);
            List<String> bindViewCodeList = bindViewCodeHolder.get(qualifiedName);
            if (bindViewCodeList != null) {
                for (String code : bindViewCodeList) {
                    bindViewBuilder.addCode(code);
                }
            }

            Map<String, List<String>> stringListMap = onClickHolder.get(qualifiedName);
            if (stringListMap != null) {
                for (String methodName : stringListMap.keySet()) {
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

            MethodSpec.Builder bindBundleBuilder = createBindBundleMethod(className);
            List<String> bindBundleCodeList = bindBundleHolder.get(qualifiedName);
            if (bindBundleCodeList != null) {
                for (String code : bindBundleCodeList) {
                    bindBundleBuilder.addCode(code);
                }
            }
            typeSpecBuilder.addMethod(bindBundleBuilder.build());

            try {
                JavaFile javaFile = JavaFile.builder(className.packageName(), typeSpecBuilder.build()).build();
                javaFile.writeTo(mProcess.getProcessingEnv().getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * *     private class QsAnnotationExecutor implements AnnotationExecutor<MainActivity> {
     * *
     * *         @Override public void bindView(MainActivity target, Object obj) {
     * *             View view = (View) obj;
     * *             target.tv_300 = view.findViewById(100);
     * *         }
     * *     }
     */
    private TypeSpec.Builder generateClass(ClassName className, ClassName superClassName) {
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
        builder.addSuperinterface(ParameterizedTypeName.get(superClassName, typeName));
        return builder;
    }

    private MethodSpec.Builder createBindViewMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindView");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target", Modifier.FINAL)
                .addParameter(ClassName.bestGuess("android.view.View"), "view");
        return builder;
    }


    private MethodSpec.Builder createBindBundleMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindBundle");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "target")
                .addParameter(ClassName.bestGuess("android.os.Bundle"), "bundle");
        return builder;
    }

    private class BindItem {
        private String fieldName;
        private int    viewId;

        BindItem(String fieldName, int viewId) {
            this.fieldName = fieldName;
            this.viewId = viewId;
        }
    }
}
