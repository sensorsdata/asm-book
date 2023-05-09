package cn.sensorsdata.autotrack.plugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AutoTrackPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("======plugin=====" + project.getName());
        AutoTrackExtension autoTrackExtension = project.getExtensions().create("autotrackConfig", AutoTrackExtension.class);
        System.out.println("===extension===" + autoTrackExtension);
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if (appExtension != null) {
            appExtension.registerTransform(new AutoTrackTransform());
        }
        AutoTrackHelper.getHelper().autoTrackExtension = autoTrackExtension;
        AutoTrackHelper.getHelper().appExtension = appExtension;

        project.afterEvaluate(proj -> {
            System.out.println("AutoTrack Config: " + autoTrackExtension);
        });
    }
}
