package com.qsmaxmin.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

/**
 * @CreateBy qsmaxmin
 * @Date 2018/12/17 11:52
 * @Description
 */
public class ViewHelperTask extends DefaultTask {

    @TaskAction
    def start() {
        def extension = project.extensions.findByName(ViewHelperPlugins.EXTENSION_NAME) as ViewHelperExtension
    }
}
