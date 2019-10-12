package com.qsmaxmin.plugin;

import com.qsmaxmin.qsbase.common.config.Property;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
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
class PropertyProcess {
    private final QsAnnotationProcess mProcess;
    private       String              bindConfigMethodName = "bindConfig";
    private       String              commitMethodName     = "commit";
    private       String              clearMethodName      = "clear";

    PropertyProcess(QsAnnotationProcess process) {
        this.mProcess = process;
    }

    void process(RoundEnvironment roundEnv) {
        Set<? extends Element> propertyElement = roundEnv.getElementsAnnotatedWith(Property.class);
        if (propertyElement != null && !propertyElement.isEmpty()) {

            mProcess.printMessage("...@Property element size:" + propertyElement.size());
            ClassName superClassName = ClassName.bestGuess("com.qsmaxmin.qsbase.common.config.PropertiesExecutor");

            List<String> qualifiedNameList = new ArrayList<>();
            HashMap<String, Boolean> stringBooleanHashMap = new HashMap<>();
            HashMap<String, HashMap<String, List<String>>> propertyCodeHolder = new HashMap<>();

            for (Element element : propertyElement) {
                if (element.getKind() != ElementKind.FIELD) continue;
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                String qualifiedName = enclosingElement.getQualifiedName().toString();
                if (!qualifiedNameList.contains(qualifiedName)) qualifiedNameList.add(qualifiedName);

                HashMap<String, List<String>> stringListHashMap = propertyCodeHolder.get(qualifiedName);
                if (stringListHashMap == null) {
                    stringListHashMap = new HashMap<>();
                    propertyCodeHolder.put(qualifiedName, stringListHashMap);
                }

                //bindConfig method code
                List<String> bindConfigCodeList = stringListHashMap.get(bindConfigMethodName);
                if (bindConfigCodeList == null) {
                    bindConfigCodeList = new ArrayList<>();
                    stringListHashMap.put(bindConfigMethodName, bindConfigCodeList);
                }
                String elementName = element.getSimpleName().toString();
                String elementType = element.asType().toString();
                bindConfigCodeList.add("Object " + elementName + " = spAll.get(\"" + elementName + "\");\n");
                if (isCommonType(elementType)) {
                    String instanceType = getInstanceType(elementType);
                    if ("short".equals(elementType) || "java.lang.Short".equals(elementType)
                            || "byte".equals(elementType) || "java.lang.Byte".equals(elementType)
                            || "char".equals(elementType) || "java.lang.Character".equals(elementType)) {
                        String simpleType = getSimpleType(elementType);
                        bindConfigCodeList.add("if(" + elementName + " instanceof java.lang.Integer){\n");
                        bindConfigCodeList.add("    int " + elementName + "_int = (int) " + elementName + ";\n");
                        bindConfigCodeList.add("    config." + elementName + " = (" + simpleType + ") " + elementName + "_int;\n}\n");

                    } else if ("double".equals(elementType) || "java.lang.Double".equals(elementType)) {
                        String simpleType = getSimpleType(elementType);
                        bindConfigCodeList.add("if(" + elementName + " instanceof java.lang.Float){\n");
                        bindConfigCodeList.add("    float " + elementName + "_float = (float) " + elementName + ";\n");
                        bindConfigCodeList.add("    config." + elementName + " = (" + simpleType + ") " + elementName + "_float;\n}\n");

                    } else {
                        bindConfigCodeList.add("if (" + elementName + " instanceof " + instanceType + ") config." + elementName + " = (" + elementType + ") " + elementName + ";\n\n");
                    }
                } else {
                    stringBooleanHashMap.put(qualifiedName, true);
                    int index = elementType.indexOf('<');
                    String elementTypeNoGenericParadigm = index > 0 ? elementType.substring(0, index) : elementType;
                    bindConfigCodeList.add("if (" + elementName + " instanceof java.lang.String) {\n");
                    bindConfigCodeList.add("    if (gson == null) gson = new com.google.gson.Gson();\n");
                    bindConfigCodeList.add("    config." + elementName + " = gson.fromJson((java.lang.String) " + elementName + ", " + elementTypeNoGenericParadigm + ".class);\n}\n");
                }

                //commit method code
                List<String> commitCodeList = stringListHashMap.get(commitMethodName);
                if (commitCodeList == null) {
                    commitCodeList = new ArrayList<>();
                    stringListHashMap.put(commitMethodName, commitCodeList);
                }
                String spCommitMethodName = getSPCommitMethodName(elementType);
                if (isCommonType(elementType)) {
                    if ("double".equals(elementType)) {
                        commitCodeList.add("edit." + spCommitMethodName + "(\"" + elementName + "\", (float)config." + elementName + ");\n");
                        mProcess.printMessage("warning........class:" + qualifiedName + ", field:\"" + elementName + "\" is double type, converting to float type and saving will lose accuracy");
                    } else if ("java.lang.Double".equals(elementType)) {
                        commitCodeList.add("if(config." + elementName + " != null){\n");
                        commitCodeList.add("    double " + elementName + "_ = config." + elementName + ";\n");
                        commitCodeList.add("    edit." + spCommitMethodName + "(\"" + elementName + "\", (float)" + elementName + "_);\n");
                        commitCodeList.add("} else {\n    edit.remove(\"" + elementName + "\");\n}\n");
                        mProcess.printMessage("warning........class:" + qualifiedName + ", field:\"" + elementName + "\" is double type, converting to float type and saving will lose accuracy");
                    } else {
                        if (isCommonSimpleType(elementType)) {
                            commitCodeList.add("edit." + spCommitMethodName + "(\"" + elementName + "\", config." + elementName + ");\n");
                        } else {
                            commitCodeList.add("if(config." + elementName + " != null){\n");
                            commitCodeList.add("    edit." + spCommitMethodName + "(\"" + elementName + "\", config." + elementName + ");\n");
                            commitCodeList.add("} else {\n    edit.remove(\"" + elementName + "\");\n}\n");
                        }
                    }
                } else {
                    stringBooleanHashMap.put(qualifiedName, true);
                    commitCodeList.add("if (config." + elementName + " != null) {\n");
                    commitCodeList.add("    if (gson == null) gson = new com.google.gson.Gson();\n");
                    commitCodeList.add("    edit.putString(\"" + elementName + "\", gson.toJson(config." + elementName + "));\n ");
                    commitCodeList.add("} else {\n    edit.remove(\"" + elementName + "\");\n}\n");
                }
            }


            //create class logic
            for (String qualifiedName : qualifiedNameList) {
                ClassName className = ClassName.bestGuess(qualifiedName);
                HashMap<String, List<String>> stringListHashMap = propertyCodeHolder.get(qualifiedName);

                TypeSpec.Builder typeSpecBuilder = generateClass(className, superClassName);
                Boolean hasGson = stringBooleanHashMap.get(qualifiedName);
                if (hasGson != null && hasGson) {
                    typeSpecBuilder.addField(ClassName.bestGuess("com.google.gson.Gson"), "gson");
                }
                //create bindConfig method
                MethodSpec.Builder bindConfigMethod = createBindConfigMethod(className);
                bindConfigMethod.addCode("java.util.Map<String, ?> spAll = sp.getAll();\n");
                bindConfigMethod.addCode("if (spAll == null || spAll.isEmpty()) return;\n");
                List<String> bindConfigCodeList = stringListHashMap.get(bindConfigMethodName);
                if (bindConfigCodeList != null) {
                    for (String code : bindConfigCodeList) {
                        bindConfigMethod.addCode(code);
                    }
                }
                typeSpecBuilder.addMethod(bindConfigMethod.build());

                //create commit method
                MethodSpec.Builder commitMethod = createCommitMethod(className);
                commitMethod.addCode("SharedPreferences.Editor edit = sp.edit();\n");
                List<String> commitCodeList = stringListHashMap.get(commitMethodName);
                if (commitCodeList != null) {
                    for (String code : commitCodeList) {
                        commitMethod.addCode(code);
                    }
                }
                commitMethod.addCode("edit.apply();\n");
                typeSpecBuilder.addMethod(commitMethod.build());

                //create clear method
                MethodSpec.Builder clearMethod = createClearMethod(className);
                clearMethod.addCode("SharedPreferences.Editor edit = sp.edit();\n");
                clearMethod.addCode("edit.clear();\n");
                clearMethod.addCode("edit.apply();\n");
                typeSpecBuilder.addMethod(clearMethod.build());

                try {
                    JavaFile javaFile = JavaFile.builder(className.packageName(), typeSpecBuilder.build()).build();
                    javaFile.writeTo(mProcess.getProcessingEnv().getFiler());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    private boolean isCommonSimpleType(String typeStr) {
        return "int".equals(typeStr)
                || "boolean".equals(typeStr)
                || "long".equals(typeStr)
                || "short".equals(typeStr)
                || "byte".equals(typeStr)
                || "float".equals(typeStr)
                || "double".equals(typeStr)
                || "char".equals(typeStr);
    }

    private String getSimpleType(String typeStr) {
        switch (typeStr) {
            case "java.lang.Integer":
                return "int";
            case "java.lang.Short":
                return "short";
            case "java.lang.Byte":
                return "byte";
            case "java.lang.Character":
                return "char";
            case "java.lang.Boolean":
                return "boolean";
            case "java.lang.Float":
                return "float";
            case "java.lang.Double":
                return "double";
            case "java.lang.Long":
                return "long";
        }
        return typeStr;
    }

    private String getSPCommitMethodName(String typeStr) {
        switch (typeStr) {
            case "java.lang.String":
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
            case "double":
            case "java.lang.Float":
            case "java.lang.Double":
                return "putFloat";

            case "long":
            case "java.lang.Long":
                return "putLong";
        }
        return "putString";
    }

    private String getInstanceType(String typeStr) {
        switch (typeStr) {
            case "int":
            case "short":
            case "byte":
            case "char":
                return "java.lang.Integer";
            case "boolean":
                return "java.lang.Boolean";
            case "float":
            case "double":
                return "java.lang.Float";
            case "long":
                return "java.lang.Long";
        }
        return typeStr;
    }

    private TypeSpec.Builder generateClass(ClassName className, ClassName superClassName) {
        String simpleName = className.simpleName();
        String extraName = "_QsAnn";
        String realClassName = simpleName + extraName;
        mProcess.printMessage("generateClass.......class:" + className + extraName);

        TypeSpec.Builder builder = TypeSpec.classBuilder(realClassName).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ClassName typeName = ClassName.bestGuess(className.packageName() + "." + simpleName);
        builder.addSuperinterface(ParameterizedTypeName.get(superClassName, typeName));
        return builder;
    }

    private MethodSpec.Builder createBindConfigMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(bindConfigMethodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "config")
                .addParameter(ClassName.bestGuess("android.content.SharedPreferences"), "sp");
        return builder;
    }

    private MethodSpec.Builder createCommitMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(commitMethodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "config")
                .addParameter(ClassName.bestGuess("android.content.SharedPreferences"), "sp");
        return builder;
    }

    private MethodSpec.Builder createClearMethod(TypeName target) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(clearMethodName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(void.class)
                .addParameter(target, "config")
                .addParameter(ClassName.bestGuess("android.content.SharedPreferences"), "sp");
        return builder;
    }
}
