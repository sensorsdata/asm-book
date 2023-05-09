package cn.sensorsdata.asmbook.buildsrc;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class CopyFileTransform extends Transform {
    @Override
    public String getName() {
        return "chapter2_03";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(); //此处返回空的集合
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("=====start transform=====");

        //测试  getReferencedScopes
        transformInvocation.getReferencedInputs().stream().forEach(transformInput -> {

            Collection<DirectoryInput> directoryInputs = transformInput.getDirectoryInputs();
            Collection<JarInput> jarInputs = transformInput.getJarInputs();
            System.out.println("directoryInputs size: " + directoryInputs.size() + "===jarInputs size: " + jarInputs.size() + "===");

            transformInput.getDirectoryInputs().stream().forEach(directoryInput -> {
                System.out.println("directoryInput=====" + directoryInput.getFile());
            });

            transformInput.getJarInputs().stream().forEach(jarInput -> {
                System.out.println("jarInput=====" +jarInput.getFile());
            });
        });
    }

    @Override
    public boolean isIncremental() {
        return false;
    }
}
