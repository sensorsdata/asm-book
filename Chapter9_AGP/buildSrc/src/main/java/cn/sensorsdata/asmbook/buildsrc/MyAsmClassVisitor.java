package cn.sensorsdata.asmbook.buildsrc;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.android.build.api.instrumentation.InstrumentationContext;
import com.android.build.api.instrumentation.InstrumentationParameters;

import org.gradle.api.provider.Property;
import org.objectweb.asm.ClassVisitor;

public abstract class MyAsmClassVisitor implements AsmClassVisitorFactory<InstrumentationParameters.None> {


    @Override
    public ClassVisitor createClassVisitor( ClassContext classContext,ClassVisitor classVisitor) {
        int b = 100;

        return new MyClassVisitor(classVisitor);
    }

    @Override
    public boolean isInstrumentable( ClassData classData) {

        return true;
    }
}

