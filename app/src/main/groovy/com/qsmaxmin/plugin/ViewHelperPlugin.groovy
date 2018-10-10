import org.gradle.api.Plugin
import org.gradle.api.Project

public class ViewHelperPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task('apkdist') << {
            println 'hello, gradle......'
        }
    }
}