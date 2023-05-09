package cn.sensorsdata.autotrack.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * 普通写法的样板代码
 */
public class AutoTrackTransform extends Transform {

    @Override
    public String getName() {
        return "myAutoTrack";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }
        //获取需要消费的数据
        Collection<TransformInput> inputCollection = transformInvocation.getInputs();
        //遍历数据
        inputCollection.parallelStream().forEach((TransformInput transformInput) -> {
            //1.获取 jar 包类型的输入
            Collection<JarInput> jarInputCollection = transformInput.getJarInputs();
            jarInputCollection.parallelStream().forEach(jarInput -> {
                //处理 jar 包
                processJarFile(jarInput, transformInvocation);
            });

            //2.获取源码编译的文件夹输入
            Collection<DirectoryInput> directoryInputCollection = transformInput.getDirectoryInputs();
            directoryInputCollection.parallelStream().forEach(directoryInput -> {
                //处理 directory
                processDirectoryFile(directoryInput, transformInvocation);
            });
        });
    }

    /**
     * 处理 Jar 包，我们在这个方法中会获取 Jar 包中的文件，但不对其中的文件做特殊处理，而是直接返回。
     *
     * @param jarInput JarInput
     * @param transformInvocation TransformInvocation
     */
    private void processJarFile(JarInput jarInput, TransformInvocation transformInvocation) {
        //获取 Jar 包文件
        //例如：/Users/username/.gradle/caches/transforms-1/files-1.1/appcompat-1.2.0.aar/e80edd062e6d61edb3235af96b64619d/jars/classes.jar
        File file = jarInput.getFile();

        //获取输出的目标文件
        //例如：/Users/username/Documents/work/others/book-asm/Chapter2_01/app/build/intermediates/transforms/boilerplate_incremental/debug/40.jar
        File outputFile = transformInvocation.getOutputProvider().getContentLocation(file.getAbsolutePath(),
                jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

        //将数据 copy 到指定目录
        try {
            if (transformInvocation.isIncremental()) {
                switch (jarInput.getStatus()) {
                    case REMOVED:
                        FileUtils.forceDelete(outputFile);
                        break;
                    case CHANGED:
                    case ADDED:
                        //例如修改此 Jar 包文件中的 class 文件
                        File modifiedJarFile = modifyJar(file, transformInvocation.getContext().getTemporaryDir());
                        FileUtils.copyFile(modifiedJarFile != null ? modifiedJarFile : file, outputFile);
                        break;
                    case NOTCHANGED:
                        break;
                }
            } else {
                //例如修改此 Jar 包文件中的 class 文件
                File modifiedJarFile = modifyJar(file, transformInvocation.getContext().getTemporaryDir());
                FileUtils.copyFile(modifiedJarFile != null ? modifiedJarFile : file, outputFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对 Jar 包中的内容进行处理，通常是处理 class 文件
     *
     * @param file jar 包对应的 File
     * @param tempDir 输出临时文件用的文件夹
     * @return 返回修改过的 Jar 包，如果返回值是 null，表示未成功修改，在这种情况下直接 copy 原有文件即可
     */
    private File modifyJar(File file, File tempDir) {
        //这里需要进行判断，可能是一个空 Jar 包，如果是将会抛出: ZipException: zip file is empty
        if (file == null || file.length() == 0) {
            return null;
        }
        try {
            //此处创建 JarFile 对象，注意第二个参数是 false，表示我们不校验 jar 包的签名信息
            JarFile jarFile = new JarFile(file, false);
            //为了防止重名导致覆盖，我们这里取文件 md5 的前 8 位
            String tmpNameHex = DigestUtils.md5Hex(file.getAbsolutePath()).substring(0, 8);
            File outputJarFile = new File(tempDir, tmpNameHex + file.getName());

            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJarFile));

            //处理 Jar 包中的内容
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while ((enumeration.hasMoreElements())) {
                JarEntry jarEntry = enumeration.nextElement();
                String entryName = jarEntry.getName();

                //如果有签名文件，我们忽略签名文件
                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                    //do nothing 什么都不做
                } else {
                    //创建一个新的 entry，我们会将修改后的内容放在此 entry 中
                    JarEntry outputEntry = new JarEntry(entryName);
                    //开始写入一个新的 Jar File entry
                    jarOutputStream.putNextEntry(outputEntry);
                    //获取对应 entry 的输入流，我们读取其中的的字节数据，后面需要使用
                    try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                        //获取原 entry 数据
                        byte[] sourceBytes = toByteArrayAndAutoCloseStream(inputStream);
                        byte[] outputBytes = null;
                        //判断是否是 class 文件，如果是的话，我们就处理对其原始数据进行处理
                        //比如我们可以使用 ASM、Javassist 对其进行修改
                        if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                            outputBytes = handleBytes(sourceBytes, entryName);
                        }
                        jarOutputStream.write(outputBytes == null ? sourceBytes : outputBytes);
                        //结束写入当前的 entry，可以开启下一个 entry
                        jarOutputStream.closeEntry();

                    } catch (Exception e) {
                        System.err.println("Exception encountered while processing jar: " + file.getAbsolutePath());
                        IOUtils.closeQuietly(jarFile);
                        IOUtils.closeQuietly(jarOutputStream);
                        e.printStackTrace();
                        return null;
                    }
                }
            }

            IOUtils.closeQuietly(jarFile);
            IOUtils.closeQuietly(jarOutputStream);
            return outputJarFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 用户可以再这里实现具体的处理原始数据的逻辑，例如使用 ASM、Javassit 等工具修改 class 文件，然后返回处理后的结果。
     * 此方法直接返回了输入的值。
     *
     * @param data 原始数据
     * @return 修改后的数据
     */
    private byte[] handleBytes(byte[] data, String canonicalName) {
        try {
            //如果在黑名单中就直接返回不处理
            if (checkBlackList(canonicalName)) {
                return data;
            }

            ClassReader classReader = new ClassReader(data);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            //为方便阅读和展示，以下的 ClassVisitor 以及 AdviceAdapter 都使用匿名类实现。
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM7, classWriter) {
                private String className;
                private String superName;
                private List<String> interfaceList;

                boolean isTracked = false;

                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    this.className = name;
                    this.superName = superName;
                    this.interfaceList = Arrays.asList(interfaces);
                    //System.out.println("visit==="+name+"===="+Arrays.toString(interfaces));
                }

                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if ("Lcn/sensorsdata/autotrack/sdk/AutoTrackInstrumented;".equals(descriptor)) {
                        System.out.println("descriptor===" + descriptor + "===" + visible);
                        isTracked = true;
                    }
                    return super.visitAnnotation(descriptor, visible);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (isTracked) {
                        return methodVisitor;
                    }

                    //当方法名、方法签名以及实现了 View$OnClickListener 接口时才去处理
                    if ("onClick".equals(name)
                            && "(Landroid/view/View;)V".equals(descriptor)
                            && interfaceList.contains("android/view/View$OnClickListener")) {
                        methodVisitor = new AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {

                            int viewPosition = -1;

                            @Override
                            protected void onMethodEnter() {
                                super.onMethodEnter();
                                viewPosition = newLocal(Type.getType("Landroid/view/View;"));
                                this.mv.visitVarInsn(Opcodes.ALOAD, 1);
                                this.mv.visitVarInsn(Opcodes.ASTORE, viewPosition);
                            }

                            @Override
                            protected void onMethodExit(int opcode) {
                                super.onMethodExit(opcode);

                                //在退出方法时添加 hook 方法
                                //加载 hook 方法需要的参数
                                if (viewPosition != -1) {
                                    this.mv.visitVarInsn(Opcodes.ALOAD, viewPosition);
                                } else {
                                    this.mv.visitVarInsn(Opcodes.ALOAD, 1);
                                }
                                this.mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/sensorsdata/autotrack/sdk/TrackHelper",
                                        "trackClick", "(Landroid/view/View;)V", false);
                            }
                        };
                    }
                    return methodVisitor;
                }

                @Override
                public void visitEnd() {
                    super.visitEnd();
                    if (!isTracked) {
                        this.cv.visitAnnotation("Lcn/sensorsdata/autotrack/sdk/AutoTrackInstrumented;", false);
                    }
                }
            };
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
            return classWriter.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return data;
        }
    }

    /**
     * 判断类是否在黑名单中
     */
    private boolean checkBlackList(final String canonicalName) {
        if (canonicalName != null) {
            String tmp = canonicalName.replace(File.separator, ".");
            return AutoTrackHelper.getHelper().autoTrackExtension.exclude.parallelStream().anyMatch(tmp::startsWith);
        }
        return false;
    }

    /**
     * 对工程源码中生成的 class 文件进行处理
     *
     * @param directoryInput DirectoryInput
     * @param transformInvocation TransformInvocation
     */
    private void processDirectoryFile(DirectoryInput directoryInput, TransformInvocation transformInvocation) {
        //获取源码编译后对应的文件夹
        //例如：/Users/username/Documents/work/others/book-asm/Chapter2_01/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes
        File srcDir = directoryInput.getFile();
        System.out.println("src dir====" + srcDir);
        //获取输出的目标
        //例如：/Users/username/Documents/work/others/book-asm/Chapter2_01/app/build/intermediates/transforms/boilerplate_incremental/debug/1
        File outputDir = transformInvocation.getOutputProvider().getContentLocation(srcDir.getAbsolutePath(),
                directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
        //将数据 copy 到指定目录
        try {
            //outputDir 不存在，需要创建
            FileUtils.forceMkdir(outputDir);
            System.out.println("output dir====" + outputDir);

            //增量编译
            if (transformInvocation.isIncremental()) {
                System.out.println("======start incremental======");
                Map<File, Status> changedFileMap = directoryInput.getChangedFiles();
                changedFileMap.forEach((file, status) -> {
                    //获取变动的文件对应在 output directory 中的位置
                    String destFilePath = outputDir.getAbsolutePath() + file.getAbsolutePath().replace(srcDir.getAbsolutePath(), "");
                    File destFile = new File(destFilePath);
                    System.out.println(status + "===destFilePath====" + destFilePath);
                    try {
                        switch (status) {
                            case REMOVED:
                                FileUtils.forceDelete(destFile);
                                break;
                            case ADDED:
                            case CHANGED:
                                //将修改的文件拷贝到指定位置
                                FileUtils.copyFile(file, destFile);
                                //获取文件的原始数据
                                byte[] sourceBytes = FileUtils.readFileToByteArray(destFile);
                                //对原始 class 数据进行修改
                                byte[] modifiedBytes = handleBytes(sourceBytes, destFilePath.replace(outputDir.getAbsolutePath() + File.separator, ""));
                                //如果修改了数据，就将新数据保存到元文件中
                                if (modifiedBytes != null) {
                                    FileUtils.writeByteArrayToFile(destFile, modifiedBytes, false);
                                }
                                break;
                            case NOTCHANGED:
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            //非增量编译
            else {
                //将数据拷贝到输出目录，然后对输出目录中的 class 文件进行处理
                FileUtils.copyDirectory(srcDir, outputDir);
                //遍历输出目录中的所有的 class 文件，包括子目录
                FileUtils.listFiles(outputDir, new String[]{"class"}, true).parallelStream().forEach(clazzFile -> {
                    try {
                        //获取文件的原始数据
                        byte[] sourceBytes = FileUtils.readFileToByteArray(clazzFile);
                        //对原始 class 数据进行修改
                        byte[] modifiedBytes = handleBytes(sourceBytes,
                                clazzFile.getAbsolutePath().replace(outputDir.getAbsolutePath() + File.separator, ""));
                        //如果修改了数据，就将新数据保存到元文件中
                        if (modifiedBytes != null) {
                            FileUtils.writeByteArrayToFile(clazzFile, modifiedBytes, false);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将输入流转换成 byte 数组
     */
    static byte[] toByteArrayAndAutoCloseStream(InputStream input) throws Exception {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
            return output.toByteArray();
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
        }
    }
}