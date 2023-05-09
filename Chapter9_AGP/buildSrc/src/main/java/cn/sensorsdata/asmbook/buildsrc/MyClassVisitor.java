package cn.sensorsdata.asmbook.buildsrc;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class MyClassVisitor extends ClassVisitor {

    public MyClassVisitor( ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
