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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class CopyFileTransform extends Transform {
    @Override
    public String getName() {
        return "chapter2_02";
    }

    //定义消费类型
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    //定义消费作用域
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    //是否支持增量编译
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println("=====start transform=====");
        //获取需要消费的数据
        Collection<TransformInput> inputCollection = transformInvocation.getInputs();
        //遍历数据
        inputCollection.parallelStream().forEach((TransformInput transformInput) -> {
            //1.获取 jar 包类型的输入
            Collection<JarInput> jarInputCollection = transformInput.getJarInputs();
            jarInputCollection.parallelStream().forEach(jarInput -> {
                //获取 Jar 包文件
                File file = jarInput.getFile();

                //获取输出的目标文件
                File outputFile = transformInvocation.getOutputProvider().getContentLocation(file.getAbsolutePath(),
                       jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                //将数据 copy 到指定目录
                try {
                    FileUtils.copyFile(file, outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            //2.获取源码编译的文件夹输入
            Collection<DirectoryInput> directoryInputCollection = transformInput.getDirectoryInputs();
            directoryInputCollection.parallelStream().forEach(directoryInput -> {
                //获取源码编译后对应的文件夹
                File file = directoryInput.getFile();
                //获取输出的目标
                File outputDir = transformInvocation.getOutputProvider().getContentLocation(file.getAbsolutePath(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                //将数据 copy 到指定目录
                try {
                    //outputDir 不存在，需要创建
                    FileUtils.forceMkdir(outputDir);
                    FileUtils.copyDirectory(file, outputDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
