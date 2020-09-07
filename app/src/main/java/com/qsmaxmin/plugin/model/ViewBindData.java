package com.qsmaxmin.plugin.model;

import com.qsmaxmin.annotation.bind.Bind;
import com.qsmaxmin.annotation.bind.BindBundle;
import com.qsmaxmin.annotation.bind.OnClick;
import com.qsmaxmin.plugin.QsAnnotationProcess;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.HashMap;
import java.util.HashSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * @CreateBy administrator
 * @Date 2020/8/19 11:56
 * @Description
 */
public class ViewBindData extends BaseData {
    private static final String CLASS_PLUGIN_HELPER = "com.qsmaxmin.qsbase.plugin.QsPluginHelper";
    private static final String METHOD_CAST_OBJECT  = "forceCastObject";
    private static final String METHOD_CAST_VIEW    = "forceCastToView";

    private HashMap<Element, Bind>              bindHashMap;
    private HashMap<ExecutableElement, OnClick> onClickHashMap;
    private HashMap<Element, BindBundle>        bindBundleHashMap;
    private ClassName                           helperClass;

    public ViewBindData(QsAnnotationProcess process, String qualifiedName) {
        super(process, qualifiedName);
        this.helperClass = ClassName.bestGuess(CLASS_PLUGIN_HELPER);
    }

    public void addBindData(Element element, Bind bind) {
        if (bindHashMap == null) bindHashMap = new HashMap<>();
        bindHashMap.put(element, bind);
    }

    public void addOnClickData(ExecutableElement element, OnClick onClick) {
        if (onClickHashMap == null) onClickHashMap = new HashMap<>();
        onClickHashMap.put(element, onClick);
    }

    public void addBindBundleData(Element element, BindBundle bindBundle) {
        if (bindBundleHashMap == null) bindBundleHashMap = new HashMap<>();
        bindBundleHashMap.put(element, bindBundle);
    }

    private boolean shouldGenerateViewBindMethod() {
        return (bindHashMap != null && bindHashMap.size() > 0)
                || (onClickHashMap != null && onClickHashMap.size() > 0);
    }

    private boolean shouldGenerateBindBundleMethod() {
        return bindBundleHashMap != null && bindBundleHashMap.size() > 0;
    }

    private void addViewBindCode(MethodSpec.Builder builder) {
        HashSet<Integer> totalViewIds = null;
        if (bindHashMap != null) {
            totalViewIds = new HashSet<>();
            builder.addCode("//find views id base on @Bind annotation\n");
            for (Element element : bindHashMap.keySet()) {
                Bind bind = bindHashMap.get(element);
                int viewId = bind.value();
                totalViewIds.add(viewId);

                builder.addCode("View v_" + viewId + " = view.findViewById(" + viewId + ");\n");
                builder.addCode("if(v_" + viewId + " != null) target." + element.getSimpleName().toString() + " = $T." + METHOD_CAST_VIEW + "(v_" + viewId + ");\n", helperClass);
            }
        }

        if (onClickHashMap != null) {
            if (totalViewIds == null) totalViewIds = new HashSet<>();
            builder.addCode("\n//find views id base on @OnClick annotation\n");
            for (ExecutableElement element : onClickHashMap.keySet()) {
                OnClick onClick = onClickHashMap.get(element);
                int[] ids = onClick.value();
                for (int viewId : ids) {
                    if (!totalViewIds.contains(viewId)) {
                        totalViewIds.add(viewId);
                        builder.addCode("View v_" + viewId + " = view.findViewById(" + viewId + ");\n");
                    }
                }
            }

            for (ExecutableElement element : onClickHashMap.keySet()) {
                OnClick onClick = onClickHashMap.get(element);
                int[] ids = onClick.value();
                long clickInterval = onClick.clickInterval();

                String methodName = element.getSimpleName().toString();

                builder.addCode("\n//set click listener base on @OnClick annotation, method name:" + methodName + "\n");

                builder.addCode("View.OnClickListener " + methodName + "Listener = new View.OnClickListener() {\n"
                        + "   @Override public void onClick(View v) {\n"
                        + "       if ($T.isFastDoubleClick(" + clickInterval + ")) return;\n"
                        + "       target." + methodName + "(v);\n"
                        + "   }\n"
                        + "};\n", helperClass);

                for (int viewId : ids) {
                    builder.addCode("if (v_" + viewId + " != null) v_" + viewId + ".setOnClickListener(" + methodName + "Listener);\n");
                }
            }
        }
    }

    public void addBundleBindCode(MethodSpec.Builder builder) {
        builder.addCode("//bind bundle by @BindBundle annotation\n");
        builder.addCode("if(bundle == null) return;\n");
        for (Element element : bindBundleHashMap.keySet()) {
            BindBundle bindBundle = bindBundleHashMap.get(element);
            String bundleKey = bindBundle.value();
            Name simpleName = element.getSimpleName();
            builder.addCode("Object bv_" + simpleName + " = bundle.get(\"" + bundleKey + "\");\n");
            builder.addCode("if(bv_" + simpleName + " != null)target." + simpleName + " = $T." + METHOD_CAST_OBJECT + "(bv_" + simpleName + ");\n", helperClass);
        }
    }

    public ClassName getViewBindExecuteClassName() {
        return ClassName.get(getClassName().packageName(), getClassName().simpleName() + "_QsBind");
    }

    public MethodSpec generateBindViewMethod() {
        if (shouldGenerateViewBindMethod()) {
            MethodSpec.Builder builder = createBindViewMethodBuilder();
            addViewBindCode(builder);
            return builder.build();
        }
        return null;
    }

    public MethodSpec generateBindBundleMethod() {
        if (shouldGenerateBindBundleMethod()) {
            MethodSpec.Builder builder = createBindBundleMethodBuilder();
            addBundleBindCode(builder);
            return builder.build();
        }
        return null;
    }

    private MethodSpec.Builder createBindViewMethodBuilder() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindView");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(getClassName(), "target", Modifier.FINAL)
                .addParameter(ClassName.bestGuess("android.view.View"), "view");
        builder.addJavadoc("Find the corresponding control object according to the ID of the @Bind and @OnClick annotations and bind it to the current View layer\n");
        return builder;
    }

    private MethodSpec.Builder createBindBundleMethodBuilder() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bindBundle");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(getClassName(), "target")
                .addParameter(ClassName.bestGuess("android.os.Bundle"), "bundle");
        builder.addJavadoc("Find the corresponding Bundle passed value from the @BindBundle annotation string and bind it to the current View layer\n");
        return builder;
    }
}
