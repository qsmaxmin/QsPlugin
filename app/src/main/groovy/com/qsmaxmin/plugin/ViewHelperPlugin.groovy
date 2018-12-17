import org.gradle.api.Plugin
import org.gradle.api.Project

public class ViewHelperPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
//        def extension = project.extensions.create("QsBase", ViewHelperExtension)
        project.task('QsBaseTask') << {
            println 'QsBase is enable:true'
        }
    }
}