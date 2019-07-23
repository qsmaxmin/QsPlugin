package com.qsmaxmin.plugin;

import com.google.auto.service.AutoService;
import com.qsmaxmin.qsbase.common.config.Property;
import com.qsmaxmin.qsbase.common.viewbind.annotation.Bind;
import com.qsmaxmin.qsbase.common.viewbind.annotation.BindBundle;
import com.qsmaxmin.qsbase.common.viewbind.annotation.OnClick;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
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
        viewBindProcess.process(roundEnv);

        PropertyProcess propertyProcess = new PropertyProcess(this);
        propertyProcess.process(roundEnv);

        long endTime = System.currentTimeMillis();
        printMessage("end.................annotation process complete, use time:" + (endTime - startTime) + "ms");
        return true;
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

}
