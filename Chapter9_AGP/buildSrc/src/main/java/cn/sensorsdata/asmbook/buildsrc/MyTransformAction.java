package cn.sensorsdata.asmbook.buildsrc;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

public abstract class MyTransformAction implements TransformAction<TransformParameters.None> {

    @InputArtifact
    public abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(TransformOutputs outputs) {
        System.out.println("=========11111");

        int a = 10;

        Provider<FileSystemLocation> aa =  getInputArtifact();
        int b = 10;

    }
}
