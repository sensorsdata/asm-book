package cn.sensorsdata.asmbook.buildsrc;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TestTransformPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getLogger().warn("Test transform plugin");
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
        if(appExtension != null){
            appExtension.registerTransform(new MyTransform());
        }
    }
}
