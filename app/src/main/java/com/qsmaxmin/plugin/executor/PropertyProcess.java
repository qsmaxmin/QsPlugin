package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.plugin.QsAnnotationProcess;
import com.qsmaxmin.plugin.model.QualifiedItem;
import com.qsmaxmin.qsbase.common.config.Property;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/7/22 17:18
 * @Description
 */
public class PropertyProcess extends BaseProcess {
    private final ClassName superClassName;
    private final String    bindConfigMethodName = "bindConfig";
    private final String    commitMethodName     = "commit";

    public PropertyProcess(QsAnnotationProcess process, String superClassPath) {
        super(process);
        this.superClassName = ClassName.bestGuess(superClassPath);
    }

    @Override public int process(RoundEnvironment roundEnv) {
        List<QualifiedItem> qualifiedItemList = new ArrayList<>();

        Set<? extends Element> propertyElement = roundEnv.getElementsAnnotatedWith(Property.class);
        if (propertyElement == null || propertyElement.isEmpty()) return 0;
        printMessage("@Property element size:" + propertyElement.size());

        List<String> qualifiedNameList = new ArrayList<>();
        HashMap<String, HashMap<String, List<CodeBlock>>> propertyCodeHolder = new HashMap<>();

        for (Element element : propertyElement) {
            if (element.getKind() != ElementKind.FIELD) continue;
            Property property = element.getAnnotation(Property.class);
            String key = property.value();

            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String qualifiedName = enclosingElement.getQualifiedName().toString();
            if (!qualifiedNameList.contains(qualifiedName)) qualifiedNameList.add(qualifiedName);

            HashMap<String, List<CodeBlock>> stringListHashMap = propertyCodeHolder.get(qualifiedName);
            if (stringListHashMap == null) {
                stringListHashMap = new HashMap<>();
                propertyCodeHolder.put(qualifiedName, stringListHashMap);
            }

            //bindConfig method code
            List<CodeBlock> bindConfigCodeList = stringListHashMap.get(bindConfigMethodName);
            if (bindConfigCodeList == null) {
                bindConfigCodeList = new ArrayList<>();
                stringListHashMap.put(bindConfigMethodName, bindConfigCodeList);
            }
            String elementName = element.getSimpleName().toString();
            String elementType = element.asType().toString();
            if ("".equals(key)) {
                key = elementName;
            }
            if (isCommonType(elementType)) {
                String methodName;
                if (isIntType(elementType)) {
                    methodName = "forceCastInt";
                } else if (isShortType(elementType)) {
                    methodName = "forceCastToShort";
                } else if (isByteType(elementType)) {
                    methodName = "forceCastToByte";
                } else if (isCharType(elementType)) {
                    methodName = "forceCastToChar";
                } else if (isFloatType(elementType)) {
                    methodName = "forceCastToFloat";
                } else if (isDoubleType(elementType)) {
                    methodName = "forceCastToDouble";
                } else if (isBooleanType(elementType)) {
                    methodName = "forceCastToBoolean";
                } else if (isLongType(elementType)) {
                    methodName = "forceCastToLong";
                } else {
                    methodName = "forceCastObject";
                }
                bindConfigCodeList.add(CodeBlock.of("config." + elementName + " = " + methodName + "(spAll.get(\"" + key + "\"));\n"));
            } else {
                String type = elementType;
                int index = elementType.indexOf('<');
                if (index >= 0) {
                    type = elementType.substring(0, index);
                }
                bindConfigCodeList.add(CodeBlock.of("config." + elementName + " = jsonStringToObject(spAll.get(\"" + key + "\"), $T.class);\n", ClassName.bestGuess(type)));
            }

            //commit method code
            List<CodeBlock> commitCodeList = stringListHashMap.get(commitMethodName);
            if (commitCodeList == null) {
                commitCodeList = new ArrayList<>();
                stringListHashMap.put(commitMethodName, commitCodeList);
            }
            String spCommitMethodName = getSPCommitMethodName(elementType);
            if (isCommonType(elementType)) {
                if ("double".equals(elementType)) {
                    commitCodeList.add(CodeBlock.of("edit." + spCommitMethodName + "(\"" + key + "\", String.valueOf(config." + elementName + "));\n"));
                } else if ("java.lang.Double".equals(elementType)) {
                    commitCodeList.add(CodeBlock.of("edit." + spCommitMethodName + "(\"" + key + "\", doubleCastToString(config." + elementName + "));\n"));
                } else {
                    commitCodeList.add(CodeBlock.of("edit." + spCommitMethodName + "(\"" + key + "\", config." + elementName + ");\n"));
                }
            } else {
                String type = elementType;
                int index = elementType.indexOf('<');
                if (index >= 0) {
                    type = elementType.substring(0, index);
                }
                commitCodeList.add(CodeBlock.of("edit.putString(\"" + key + "\", objectToJsonString(config." + elementName + ", $T.class));\n", ClassName.bestGuess(type)));
            }
        }
        for (String qualifiedName : qualifiedNameList) {
            qualifiedItemList.add(new QualifiedItem(qualifiedName));
        }
        //create class logic
        for (QualifiedItem item : qualifiedItemList) {
            HashMap<String, List<CodeBlock>> stringListHashMap = propertyCodeHolder.get(item.getQualifiedName());

            TypeSpec.Builder typeSpecBuilder = generateClass(item);
            //create bindConfig method
            MethodSpec.Builder bindConfigMethod = createBindConfigMethod(item);
            bindConfigMethod.addCode("java.util.Map<String, ?> spAll = sp.getAll();\n");
            bindConfigMethod.addCode("if (spAll == null || spAll.isEmpty()) return;\n");
            List<CodeBlock> bindConfigCodeList = stringListHashMap.get(bindConfigMethodName);
            if (bindConfigCodeList != null) {
                for (CodeBlock code : bindConfigCodeList) {
                    bindConfigMethod.addCode(code);
                }
            }
            typeSpecBuilder.addMethod(bindConfigMethod.build());

            //create commit method
            MethodSpec.Builder commitMethod = createCommitMethod(item);
            commitMethod.addCode("SharedPreferences.Editor edit = sp.edit();\n");
            List<CodeBlock> commitCodeList = stringListHashMap.get(commitMethodName);
            if (commitCodeList != null) {
                for (CodeBlock code : commitCodeList) {
                    commitMethod.addCode(code);
                }
            }
            commitMethod.addCode("edit.apply();\n");
            typeSpecBuilder.addMethod(commitMethod.build());

            try {
                JavaFile javaFile = JavaFile.builder(item.getClassName().packageName(), typeSpecBuilder.build()).build();
                javaFile.writeTo(getProcess().getProcessingEnv().getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return qualifiedItemList.size();
    }

    private boolean isIntType(String type) {
        return "int".equals(type) || "java.lang.Integer".equals(type);
    }

    private boolean isBooleanType(String type) {
        return "boolean".equals(type) || "java.lang.Boolean".equals(type);
    }

    private boolean isLongType(String type) {
        return "long".equals(type) || "java.lang.Long".equals(type);
    }

    private boolean isShortType(String type) {
        return "short".equals(type) || "java.lang.Short".equals(type);
    }

    private boolean isByteType(String type) {
        return "byte".equals(type) || "java.lang.Byte".equals(type);
    }

    private boolean isFloatType(String type) {
        return "float".equals(type) || "java.lang.Float".equals(type);
    }

    private boolean isDoubleType(String type) {
        return "double".equals(type) || "java.lang.Double".equals(type);
    }

    private boolean isCharType(String type) {
        return "char".equals(type) || "java.lang.Character".equals(type);
    }


    private boolean isCommonType(String typeStr) {
        return "int".equals(typeStr)
                || "boolean".equals(typeStr)
                || "long".equals(typeStr)
                || "short".equals(typeStr)
                || "byte".equals(typeStr)
                || "float".equals(typeStr)
                || "double".equals(typeStr)
                || "char".equals(typeStr)
                || "java.lang.String".equals(typeStr)
                || "java.lang.Integer".equals(typeStr)
                || "java.lang.Boolean".equals(typeStr)
                || "java.lang.Long".equals(typeStr)
                || "java.lang.Short".equals(typeStr)
                || "java.lang.Byte".equals(typeStr)
                || "java.lang.Float".equals(typeStr)
                || "java.lang.Double".equals(typeStr)
                || "java.lang.Character".equals(typeStr);
    }

    private String getSPCommitMethodName(String typeStr) {
        switch (typeStr) {
            case "java.lang.String":
            case "double":
            case "java.lang.Double":
                return "putString";

            case "int":
            case "short":
            case "byte":
            case "char":
            case "java.lang.Integer":
            case "java.lang.Short":
            case "java.lang.Byte":
            case "java.lang.Character":
                return "putInt";

            case "java.lang.Boolean":
            case "boolean":
                return "putBoolean";

            case "float":
            case "java.lang.Float":
                return "putFloat";

            case "long":
            case "java.lang.Long":
                return "putLong";
        }
        return "putString";
    }

    private TypeSpec.Builder generateClass(QualifiedItem item) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(item.getPropertyExecuteClassName()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.superclass(ParameterizedTypeName.get(superClassName, item.getClassName()));
        return builder;
    }

    private MethodSpec.Builder createBindConfigMethod(QualifiedItem item) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(bindConfigMethodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(item.getClassName(), "config")
                .addParameter(ClassName.bestGuess("android.content.SharedPreferences"), "sp");
        return builder;
    }

    private MethodSpec.Builder createCommitMethod(QualifiedItem item) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(commitMethodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(item.getClassName(), "config")
                .addParameter(ClassName.bestGuess("android.content.SharedPreferences"), "sp");
        return builder;
    }
}
