package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.plugin.QsAnnotationProcess;
import com.qsmaxmin.plugin.model.QualifiedItem;
import com.qsmaxmin.qsbase.common.event.Subscribe;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/10/31 14:30
 * @Description
 */
public class EventProcess extends BaseProcess {

    public EventProcess(QsAnnotationProcess mProcess, String superClassPath) {
        super(mProcess);
    }

    @Override public int process(RoundEnvironment roundEnv) {
        List<QualifiedItem> qualifiedItemList = new ArrayList<>();

        Set<? extends Element> propertyElement = roundEnv.getElementsAnnotatedWith(Subscribe.class);
        if (propertyElement == null || propertyElement.isEmpty()) return 0;
        printMessage("@Subscribe element size:" + propertyElement.size());

        List<String> qualifiedNameList = new ArrayList<>();
        HashMap<String, List<ExecutableElement>> subscribeElements = new HashMap<>();

        for (Element element : propertyElement) {
            if (element.getKind() != ElementKind.METHOD) continue;
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String qualifiedName = enclosingElement.getQualifiedName().toString();
            if (!qualifiedNameList.contains(qualifiedName)) {
                qualifiedNameList.add(qualifiedName);
                subscribeElements.put(qualifiedName, new ArrayList<ExecutableElement>());
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            List<? extends VariableElement> parameters = executableElement.getParameters();
            if (parameters != null && parameters.size() == 1) {
                List<ExecutableElement> paramNames = subscribeElements.get(qualifiedName);
                paramNames.add(executableElement);
            }
        }

        for (String qualifiedName : qualifiedNameList) {
            List<ExecutableElement> elementList = subscribeElements.get(qualifiedName);
            if (elementList == null || elementList.isEmpty()) continue;

            QualifiedItem item = new QualifiedItem(qualifiedName);
            qualifiedItemList.add(item);

            TypeSpec.Builder typeSpecBuilder = generateClass(item);


            MethodSpec.Builder eventMethodBuilder = createGetEventMethod(elementList);
            typeSpecBuilder.addMethod(eventMethodBuilder.build());

            MethodSpec.Builder executeMethod = createExecuteMethod(elementList);
            typeSpecBuilder.addMethod(executeMethod.build());
            try {
                JavaFile javaFile = JavaFile.builder(item.getClassName().packageName(), typeSpecBuilder.build()).build();
                javaFile.writeTo(getProcess().getProcessingEnv().getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return qualifiedItemList.size();
    }


    private ClassName superClassName = ClassName.bestGuess("com.qsmaxmin.qsbase.common.event.EventExecutor");

    private TypeSpec.Builder generateClass(QualifiedItem item) {
        printMessage("generateClass.......class:" + item.getQualifiedName());
        TypeSpec.Builder builder = TypeSpec.classBuilder(item.getEventExecuteClassName()).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.superclass(ParameterizedTypeName.get(superClassName, item.getClassName()));

        MethodSpec.Builder constBuilder = MethodSpec.constructorBuilder();
        constBuilder.addModifiers(Modifier.PUBLIC);
        constBuilder.addParameter(item.getClassName(), "target");
        constBuilder.addCode("super(target);\n");

        builder.addMethod(constBuilder.build());
        return builder;
    }

    private MethodSpec.Builder createGetEventMethod(List<ExecutableElement> elementList) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getEvents");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(Class[].class);

        builder.addCode("if (events == null) {\n");
        StringBuilder sb0 = new StringBuilder("\tevents = new Class[]{");
        Object[] typeNames = new TypeName[elementList.size()];
        for (int i = 0; i < elementList.size(); i++) {
            ExecutableElement executableElement = elementList.get(i);
            VariableElement variableElement = executableElement.getParameters().get(0);
            TypeMirror typeMirror = variableElement.asType();
            TypeName typeName = ClassName.get(typeMirror);
            sb0.append(i == typeNames.length - 1 ? "$T.class};\n" : "$T.class, ");
            typeNames[i] = typeName;
        }
        builder.addCode(sb0.toString(), typeNames);
        builder.addCode("}\nreturn events;\n");
        return builder;
    }


    private MethodSpec.Builder createExecuteMethod(List<ExecutableElement> elementList) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("execute");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addParameter(Object.class, "event")
                .returns(void.class);
        builder.addCode("String className = event.getClass().getName();\n");
        builder.addCode("int index = className.indexOf('$N');\n", "$");
        builder.addCode("if (index != -1) {\n");
        builder.addCode("\tclassName = className.substring(0, index) + '.' + className.substring(index + 1);\n}\n");
        builder.addCode("switch (className) {\n");

        for (int i = 0; i < elementList.size(); i++) {
            ExecutableElement executableElement = elementList.get(i);
            VariableElement variableElement = executableElement.getParameters().get(0);
            TypeMirror typeMirror = variableElement.asType();
            TypeName typeName = ClassName.get(typeMirror);

            builder.addCode("\tcase \"" + typeMirror.toString() + "\":\n");
            builder.addCode("\t\tt." + executableElement.getSimpleName() + "(($T) event);\n\t\tbreak;\n", typeName);
        }
        builder.addCode("}\n");
        return builder;
    }
}
