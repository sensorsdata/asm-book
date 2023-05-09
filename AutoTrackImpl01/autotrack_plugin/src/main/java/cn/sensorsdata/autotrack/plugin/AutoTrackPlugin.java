package cn.sensorsdata.autotrack.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AutoTrackPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("======plugin=====" + project.getName());
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if (appExtension != null) {
            appExtension.registerTransform(new AutoTrackTransform());
        }
    }
}
