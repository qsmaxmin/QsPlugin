package com.qsmaxmin.plugin.helper;

import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 14:38
 * @Description
 */
public class CommonHelper {

    public static void generateFile(Filer filer, String filePath, String code) {
        JavaFileObject sourceFile = null;
        try {
            sourceFile = filer.createSourceFile(filePath);
            Writer writer = sourceFile.openWriter();
            writer.append(code);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            try {
                if (sourceFile != null) sourceFile.delete();
            } catch (Exception ignored) {
            }
            e.printStackTrace();
        }
    }
}
