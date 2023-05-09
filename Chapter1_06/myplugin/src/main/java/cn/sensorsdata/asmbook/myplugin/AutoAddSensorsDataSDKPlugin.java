package cn.sensorsdata.asmbook.myplugin;

import com.android.build.gradle.AppExtension;
import com.sensorsdata.analytics.android.plugin.SensorsAnalyticsSDKExtension;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.PluginManager;

public class AutoAddSensorsDataSDKPlugin implements Plugin<Project> {
    //默认下载最新的版本
    String sdkVersion = "+";

    @Override
    public void apply(Project project) {
        System.out.println("Auto Add SensorsData AutoTrack SDK");
        //创建版本号扩展
        project.getExtensions().add("sdkVersion", SDKVersionExtension.class);
        project.afterEvaluate(project1 -> {

            Plugin saPlugin = project.getPlugins().findPlugin("com.sensorsdata.analytics.android");
            if (saPlugin == null) {
                //添加神策插件依赖
                project.getPluginManager().apply("com.sensorsdata.analytics.android");
                //查找扩展并获取其中的版本号设置
                Object sdkVersionExtension = project1.getExtensions().findByName("sdkVersion");
                if (sdkVersionExtension != null) {
                    SDKVersionExtension tmp = (SDKVersionExtension) sdkVersionExtension;
                    if (tmp.version != null) {
                        sdkVersion = tmp.version;
                    }
                }
                System.out.println("====final version====" + sdkVersion);
                //添加对 SDK 的依赖
                project.getDependencies().add("implementation", "com.sensorsdata.analytics.android:SensorsAnalyticsSDK:" + sdkVersion);
            }
        });
    }
}
