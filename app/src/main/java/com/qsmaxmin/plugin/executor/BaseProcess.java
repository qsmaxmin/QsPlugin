package com.qsmaxmin.plugin.executor;

import com.qsmaxmin.plugin.QsAnnotationProcess;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 10:54
 * @Description
 */
public class BaseProcess {
    private QsAnnotationProcess mProcess;

    public BaseProcess(QsAnnotationProcess mProcess) {
        this.mProcess = mProcess;
    }

    QsAnnotationProcess getProcess() {
        return mProcess;
    }

    void printMessage(String message) {
        mProcess.printMessage("\t\t" + message);
    }
}
