package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.plugin.QsAnnotationProcess;

import java.text.SimpleDateFormat;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 10:54
 * @Description
 */
public abstract class BaseProcess {
    protected final QsAnnotationProcess mProcess;
    private final   SimpleDateFormat    dateFormat;


    BaseProcess(QsAnnotationProcess mProcess) {
        this.mProcess = mProcess;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public abstract int process(RoundEnvironment roundEnv);

    QsAnnotationProcess getProcess() {
        return mProcess;
    }

    void printMessage(String message) {
        mProcess.printMessage(1, message);
    }

    protected String getQualifiedName(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        return enclosingElement.getQualifiedName().toString();
    }

    protected String getJavaDoc() {
        String timeStr = dateFormat.format(System.currentTimeMillis());
        return "该类由QsPlugin插件自动生成 " + timeStr + "\nQsPlugin插件需要配合QsBase框架一起使用，使用时请确保该插件版本和QsBase框架版本号一致，否则可能会出现不可预期的错误！\n";
    }
}
