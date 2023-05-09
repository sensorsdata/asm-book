package cn.sensorsdata.asmbook.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class MyBuildScrPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        System.out.println("My Second Plugin");

        Task task = project.task("getBuildDir");
        task.doLast(task1 -> System.out.println("build dir: " + project.getBuildDir()));

    }
}
