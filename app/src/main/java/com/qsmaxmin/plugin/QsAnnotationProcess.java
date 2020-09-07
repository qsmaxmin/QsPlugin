package com.qsmaxmin.plugin;

import com.google.auto.service.AutoService;
import com.qsmaxmin.annotation.bind.Bind;
import com.qsmaxmin.annotation.bind.BindBundle;
import com.qsmaxmin.annotation.bind.OnClick;
import com.qsmaxmin.plugin.executor.BaseProcess;
import com.qsmaxmin.plugin.executor.ViewBindProcess;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/6/6 11:13
 * @Description
 */
@AutoService(Processor.class)
public class QsAnnotationProcess extends AbstractProcessor {
    private static final String   TAG = "> Task :QsAnnotationProcess :";
    private              String[] ttt;

    @Override public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        ttt = new String[]{"", "\t", "\t\t", "\t\t\t", "\t\t\t\t", "\t\t\t\t\t"};
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) return true;
        printMessage(0, "process started.........");
        long st = System.currentTimeMillis();

        BaseProcess viewBindProcess = new ViewBindProcess(this);
        int viewBindCount = viewBindProcess.process(roundEnv);

        printMessage(0, "process complete.........process file count:" + viewBindCount + ", use time:" + (System.currentTimeMillis() - st) + "ms");
        return false;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Bind.class.getCanonicalName());
        hashSet.add(BindBundle.class.getCanonicalName());
        hashSet.add(OnClick.class.getCanonicalName());
        return hashSet;
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public void printMessage(int level, String message) {
        System.out.println(ttt[level] + TAG + message);
    }
}