package cn.sensorsdata.asmbook.myplugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class MyStandalonePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("My Third Plugin");

        Task task = project.task("getBuildDir");
        task.doLast(task1 -> System.out.println("build dir: " + project.getBuildDir()));
    }
}
