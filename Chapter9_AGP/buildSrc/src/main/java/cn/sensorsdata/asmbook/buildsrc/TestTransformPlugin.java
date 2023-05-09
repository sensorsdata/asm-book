package cn.sensorsdata.asmbook.buildsrc;

import com.android.build.api.instrumentation.FramesComputationMode;
import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class TestTransformPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getLogger().warn("Test transform plugin");

//        project.afterEvaluate(project1 -> project1.getDependencies().registerTransform(MyTransformAction.class, noneTransformSpec -> {
//            noneTransformSpec.getFrom().attribute(
//                    ArtifactAttributes.ARTIFACT_FORMAT,
//                    AndroidArtifacts.ArtifactType.JAR.getType()
//            );
//
//            noneTransformSpec.getTo().attribute(
//                    ArtifactAttributes.ARTIFACT_FORMAT,
//                    AndroidArtifacts.TYPE_MOCKABLE_JAR
//            );
//        }));


        ApplicationAndroidComponentsExtension androidComponentsExtension = project.getExtensions().findByType(ApplicationAndroidComponentsExtension.class);
        androidComponentsExtension.onVariants(androidComponentsExtension.selector().all(), variant -> {
            variant.transformClassesWith(MyAsmClassVisitor.class, InstrumentationScope.ALL, instrumentationParameters -> null);
            variant.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES);
        });


//        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class);
//        if(appExtension != null){
//            appExtension.registerTransform(new BoilerplateIncrementalTransform());
//            //appExtension.registerTransform(new CopyFileTransform());
//            //appExtension.registerTransform(new MyTransform());
//        }
//
////        LibraryExtension libraryExtension = project.getExtensions().findByType(LibraryExtension.class);
////        if(libraryExtension != null){
////            appExtension.registerTransform(new MyTransform());
////        }
    }
}
