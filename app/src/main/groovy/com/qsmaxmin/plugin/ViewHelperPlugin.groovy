import com.qsmaxmin.plugin.ViewHelperExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ViewHelperPlugin implements Plugin<Project> {

    static final String GROUP = 'QsBase'
    static final String EXTENSION_NAME = 'QsBase'

    @Override
    void apply(Project project) {
        def extension = project.extensions.create(EXTENSION_NAME, ViewHelperExtension)

        project.task('QsBaseTask') << {
            println 'QsBase is enable:' + extension.enable
        }
    }
}