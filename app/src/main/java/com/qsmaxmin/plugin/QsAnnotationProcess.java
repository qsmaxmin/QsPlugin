package com.qsmaxmin.plugin;

import com.google.auto.service.AutoService;
import com.qsmaxmin.plugin.executor.BaseProcess;
import com.qsmaxmin.plugin.executor.EventProcess;
import com.qsmaxmin.plugin.executor.PropertyProcess;
import com.qsmaxmin.plugin.executor.ViewBindProcess;
import com.qsmaxmin.plugin.helper.CommonHelper;
import com.qsmaxmin.qsbase.common.ann.QsAnn;
import com.qsmaxmin.qsbase.common.config.Property;
import com.qsmaxmin.qsbase.common.event.Subscribe;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * @CreateBy qsmaxmin
 * @Date 2019/6/6 11:13
 * @Description
 */
@AutoService(Processor.class)
public class QsAnnotationProcess extends AbstractProcessor {
    private static final String TAG                    = "QsAnnotationProcess:";
    private static final String PATH_VIEW_BIND_PARENT  = "com.qsmaxmin.qsbase.common.viewbind.ViewAnnotationExecutor";
    private static final String PATH_PROPERTIES_PARENT = "com.qsmaxmin.qsbase.common.config.PropertiesExecutor";
    private static final String PATH_EVENT_PARENT      = "com.qsmaxmin.qsbase.common.event.EventExecutor";

    @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) return true;
        long startTime = System.currentTimeMillis();
        printMessage("started........................ annotations size:" + annotations.size());

        Set<? extends Element> propertyElement = roundEnv.getElementsAnnotatedWith(QsAnn.class);
        if (propertyElement != null && !propertyElement.isEmpty()) {
            Element element = propertyElement.iterator().next();
            QsAnn qsAnn = element.getAnnotation(QsAnn.class);
            boolean library = qsAnn.isLibrary();
            if (library) {
                printMessage("\tis library, copy resource file......");
                CommonHelper.copyResourceFiles(getProcessingEnv(), "com/qsmaxmin/qsbase");
            }
        }

        BaseProcess viewBindProcess = new ViewBindProcess(this, PATH_VIEW_BIND_PARENT);
        int viewBindFileSize = viewBindProcess.process(roundEnv);
        printMessage("\tViewBindProcess complete, process file size:" + viewBindFileSize);

        PropertyProcess propertyProcess = new PropertyProcess(this, PATH_PROPERTIES_PARENT);
        int propertyFileSize = propertyProcess.process(roundEnv);
        printMessage("\tPropertyProcess complete, process file size:" + propertyFileSize);

        EventProcess eventProcess = new EventProcess(this, PATH_EVENT_PARENT);
        int eventFileSize = eventProcess.process(roundEnv);
        printMessage("\tEventProcess complete, process file size:" + eventFileSize);

        long endTime = System.currentTimeMillis();
        printMessage("end.........................annotation process complete, use time:" + (endTime - startTime) + "ms");
        return true;
    }


    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(QsAnn.class.getCanonicalName());
        hashSet.add(Bind.class.getCanonicalName());
        hashSet.add(BindBundle.class.getCanonicalName());
        hashSet.add(OnClick.class.getCanonicalName());
        hashSet.add(Property.class.getCanonicalName());
        hashSet.add(Subscribe.class.getCanonicalName());
        return hashSet;
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public void printMessage(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, TAG + message);
    }
}