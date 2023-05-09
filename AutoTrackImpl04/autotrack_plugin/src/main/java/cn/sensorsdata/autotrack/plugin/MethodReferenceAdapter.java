package cn.sensorsdata.autotrack.plugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodReferenceAdapter extends ClassNode {

    private final AtomicInteger counter = new AtomicInteger(0);
    private List<MethodNode> syntheticMethodList = new ArrayList<>();
    private final String MIDDLE_METHOD_SUFFIX = "$lambda$zw$";

    public MethodReferenceAdapter(ClassVisitor classVisitor) {
        super(Opcodes.ASM7);
        this.cv = classVisitor;
    }

    //判断是否处理 invokedynamic 指令
    //此处只是单纯的判断了 View.OnClickListener
    private HookType checkShouldHook(InvokeDynamicInsnNode node) {
        if ("onClick".equals(node.name)
                && "Landroid/view/View$OnClickListener;".equals(Type.getMethodType(node.desc).getReturnType().getDescriptor())) {
            if (node.bsmArgs != null && node.bsmArgs.length == 3) {
                if (node.bsmArgs[0] instanceof Type && node.bsmArgs[1] instanceof Handle) {
                    Type samType = (Type) node.bsmArgs[0];
                    Handle methodHandle = (Handle) node.bsmArgs[1];
                    if ("(Landroid/view/View;)V".equals(samType.getDescriptor())) {
                        return HookType.VIEW_CLICK;
                    }
                }
            }
        }
        return HookType.DO_NOTHING;
    }


    @Override
    public void visitEnd() {
        super.visitEnd();

        this.methods.forEach(methodNode -> {
            ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode node = iterator.next();
                if (node instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode tmpNode = (InvokeDynamicInsnNode) node;
                    //如果不需要 Hook 的 Lambda 和方法引用，就不处理
                    HookType hookType = checkShouldHook(tmpNode);
                    if (hookType == HookType.DO_NOTHING) {
                        continue;
                    }

                    //形如：(Ljava/util/Date;)Ljava/util/function/Consumer;   可以从 desc 中获取函数式接口，以及动态参数的内容。
                    //如果没有参数那么描述符的参数部分应该是空。
                    String desc = tmpNode.desc;
                    Type descType = Type.getType(desc);
                    Type samBaseType = descType.getReturnType();
                    //sam 接口名
                    String samBase = samBaseType.getDescriptor();
                    //sam 方法名
                    String samMethodName = tmpNode.name;
                    Object[] bsmArgs = tmpNode.bsmArgs;
                    //sam 方法描述符
                    Type samMethodType = (Type) bsmArgs[0];
                    //sam 实现方法实际参数描述符
                    Type implMethodType = (Type) bsmArgs[2];
                    //sam name + desc，可以用来辨别是否是需要 Hook 的 lambda 表达式
                    //例如：onClick + (View)V，根据此再加上 samBaseType 即可判断是否是需要 Hook View.OnClickListener
                    String bsmMethodNameAndDescriptor = samMethodName + samMethodType.getDescriptor();
                    //中间方法的名称: Lambda 所在的方法名 + $lambda$zw$ + 数字
                    String middleMethodName = methodNode.name + MIDDLE_METHOD_SUFFIX + counter.incrementAndGet();
                    //中间方法的描述符，例如有状态 Lambda 需要拼接额外参数 + SAM 接口的原本参数
                    String middleMethodDesc = "";
                    Type[] descArgTypes = descType.getArgumentTypes();
                    if (descArgTypes.length == 0) {
                        middleMethodDesc = implMethodType.getDescriptor();
                    } else {
                        middleMethodDesc = "(";
                        for (Type tmpType : descArgTypes) {
                            middleMethodDesc += tmpType.getDescriptor();
                        }
                        middleMethodDesc += implMethodType.getDescriptor().replace("(", "");
                    }

                    //INDY 指令原本的 handle，需要将此 handle 替换成新的 handle
                    Handle oldHandle = (Handle) bsmArgs[1];
                    Handle newHandle = new Handle(Opcodes.H_INVOKESTATIC, this.name, middleMethodName, middleMethodDesc, false);

                    InvokeDynamicInsnNode newDynamicNode = new InvokeDynamicInsnNode(tmpNode.name, tmpNode.desc, tmpNode.bsm, samMethodType, newHandle, implMethodType);
                    iterator.remove();
                    iterator.add(newDynamicNode);

                    generateMiddleMethod(oldHandle, middleMethodName, middleMethodDesc, bsmMethodNameAndDescriptor, hookType);

                }
            }
        });

        this.methods.addAll(syntheticMethodList);
        accept(cv);
    }

    private void generateMiddleMethod(Handle oldHandle, String middleMethodName, String middleMethodDesc, String bsmMethodNameAndDescriptor, HookType hookType) {
        //开始对生成的方法中插入或者调用相应的代码
        MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC /*| Opcodes.ACC_SYNTHETIC*/,
                middleMethodName, middleMethodDesc, null, null);
        methodNode.visitCode();

        //添加 Hook 代码
        addHookMethod(methodNode, middleMethodDesc, bsmMethodNameAndDescriptor, hookType);

        // 此块 tag 具体可以参考: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.invokedynamic
        int accResult = oldHandle.getTag();
        switch (accResult) {
            case Opcodes.H_INVOKEINTERFACE:
                accResult = Opcodes.INVOKEINTERFACE;
                break;
            case Opcodes.H_INVOKESPECIAL:
                //private, this, super 等会调用
                accResult = Opcodes.INVOKESPECIAL;
                break;
            case Opcodes.H_NEWINVOKESPECIAL://针对 XXX::new 的方法引用
                //constructors
                accResult = Opcodes.INVOKESPECIAL;
                methodNode.visitTypeInsn(Opcodes.NEW, oldHandle.getOwner());
                methodNode.visitInsn(Opcodes.DUP);
                break;
            case Opcodes.H_INVOKESTATIC:
                accResult = Opcodes.INVOKESTATIC;
                break;
            case Opcodes.H_INVOKEVIRTUAL:
                accResult = Opcodes.INVOKEVIRTUAL;
                break;
        }

        Type middleMethodType = Type.getType(middleMethodDesc);
        Type[] argumentsType = middleMethodType.getArgumentTypes();
        if (argumentsType.length > 0) {
            int loadIndex = 0;
            for (Type tmpType : argumentsType) {
                int opcode = tmpType.getOpcode(Opcodes.ILOAD);
                methodNode.visitVarInsn(opcode, loadIndex);
                loadIndex += tmpType.getSize();
            }
        }

        methodNode.visitMethodInsn(accResult, oldHandle.getOwner(), oldHandle.getName(), oldHandle.getDesc(), false);
        Type returnType = middleMethodType.getReturnType();
        int returnOpcodes = returnType.getOpcode(Opcodes.IRETURN);
        methodNode.visitInsn(returnOpcodes);
        methodNode.visitEnd();
        syntheticMethodList.add(methodNode);
    }

    private void addHookMethod(MethodNode node, String middleMethodDesc, String bsmMethodNameAndDescriptor, HookType hookType) {
        if (hookType == HookType.VIEW_CLICK) {
            //这里主要是处理 Lambda 有无状态的情况, 以 View.OnCLickListener#onCLick(View) 为例，
            //descriptor 的最后一个参数是 View，有状态的 Lambda 的其他参数是追加在方法前面的，
            //假如 descriptor 的值为 (long, int, View)V，我们需要计算参数 View 所在的槽。
            Type type = Type.getMethodType(middleMethodDesc);
            Type[] argTypes = type.getArgumentTypes();
            //过滤额外参数的影响，计算 SAM 方法的其实位置
            int startPosition = 0;
            //argTypes.length - 1 实际上需要根据实际的 SAM 方法中的值来决定，
            //View.OnCLickListener#onCLick(View) 方法只有一个参数，所以此处减 1
            for (int index = 0; index < argTypes.length - 1; index++) {
                startPosition += argTypes[index].getSize();
            }
            node.visitVarInsn(Opcodes.ALOAD, startPosition);
            node.visitMethodInsn(Opcodes.INVOKESTATIC, "cn/sensorsdata/autotrack/sdk/TrackHelper",
                    "trackClick", "(Landroid/view/View;)V", false);
            System.out.println("====添加 Hook");
        }
    }
}
