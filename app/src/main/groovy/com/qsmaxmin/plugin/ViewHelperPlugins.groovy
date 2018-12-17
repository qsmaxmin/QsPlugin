package com.qsmaxmin.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * @CreateBy qsmaxmin
 * @Date 2018/12/17 13:08
 * @Description
 */
public class ViewHelperPlugins implements Plugin<Project> {
    static final String GROUP = 'QsBase'
    static final String EXTENSION_NAME = 'QsBase'

    @Override
    void apply(Project target) {
        def extension = project.extensions.create(EXTENSION_NAME, ViewHelperExtension)

        project.task('QsBaseTask') << {
            println 'QsBase is enable:' + extension.enable
        }
    }
}
