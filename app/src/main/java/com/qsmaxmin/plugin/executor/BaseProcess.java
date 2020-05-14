package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.plugin.QsAnnotationProcess;

import javax.annotation.processing.RoundEnvironment;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 10:54
 * @Description
 */
public abstract class BaseProcess {
    private QsAnnotationProcess mProcess;

    BaseProcess(QsAnnotationProcess mProcess) {
        this.mProcess = mProcess;
    }

    public abstract int process(RoundEnvironment roundEnv);

    QsAnnotationProcess getProcess() {
        return mProcess;
    }

    void printMessage(String message) {
        mProcess.printMessage("\t\t" + message);
    }
}
