package com.qsmaxmin.plugin.helper;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/**
 * @CreateBy administrator
 * @Date 2020/5/13 14:38
 * @Description
 */
public class CommonHelper {
    private static CommonHelper helper = new CommonHelper();

    /**
     * 读取resource目录文件并写到指定目录
     */
    public static void generateFileByResFile(Filer filer, String filePath, String resourcePath) {
        JavaFileObject sourceFile = null;
        BufferedReader br = null;
        Writer writer = null;
        try {
            InputStream is = helper.getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);

                br = new BufferedReader(isr);
                sourceFile = filer.createSourceFile(filePath);
                writer = sourceFile.openWriter();
                String line;
                while ((line = br.readLine()) != null) {
                    writer.append(line).append('\n');
                }
                writer.flush();
            }

        } catch (Exception e) {
            if (sourceFile != null) {
                try {
                    sourceFile.delete();
                } catch (Exception ignored) {
                }
            }
            e.printStackTrace();
        } finally {
            closeStream(br);
            closeStream(writer);
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }


    public static void copyResourceFiles(ProcessingEnvironment env, String dir) {
        try {
            URL url = helper.getClass().getClassLoader().getResource(dir);
            if (url == null) return;
            String path = url.getPath();
            String jarPath = path.substring(path.indexOf('/'), path.indexOf('!'));

            JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String resourceName = jarEntry.getName();
                if (!jarEntry.isDirectory() && resourceName.startsWith(dir) && resourceName.endsWith(".java")) {
                    copyResourceFile(env.getFiler(), resourceName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void copyResourceFile(Filer filer, String resourcePath) {
        String targetPath;
        if (resourcePath.contains(".")) {
            String[] split = resourcePath.split("\\.");
            targetPath = split[0].replaceAll("/", ".");
        } else {
            targetPath = resourcePath.replaceAll("/", ".");
        }
        generateFileByResFile(filer, targetPath, resourcePath);
    }
}
