package com.github.rpc.invoke.asm;

import com.github.rpc.invoke.MethodContext;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeListener;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.*;

/**
 * 生成 MethodInvokeDispatcher 实现类
 *
 * @author Ray
 * @date created in 2022/3/3 12:40
 */
public class AsmGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AsmGenerator.class);

    private static final String CLASS_NAME = "com/github/rpc/invoke/asm/AsmMethodInvokeDispatcher";
    private static final String SUPER_CLASS = "com/github/rpc/invoke/AbstractMethodInvokeDispatcher";
    private static final String CONSTRUCTOR_DESC = "(Ljava/util/Map;Ljava/util/List;)V";
    private static final String CONSTRUCTOR_SIGN = "(Ljava/util/Map<Ljava/lang/String;" +
            "Lcom/github/rpc/invoke/MethodContext;>;Ljava/util/List<Lcom/github/rpc/invoke/MethodInvokeListener;>;)V";
    private static final String DO_INVOKE_METHOD_DESC = "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)" +
            "Ljava/lang/Object;";
    private static final String[] DO_INVOKE_METHOD_EX = new String[]{"java/lang/Throwable"};

    private final Map<String, MethodContext> methodContextMap;
    private final List<MethodInvokeListener> listeners;
    private final AsmClassLoader classLoader;
    private final ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);


    public AsmGenerator(Map<String, MethodContext> methodContextMap,
                        List<MethodInvokeListener> listeners,
                        boolean saveByteCode) {
        this.methodContextMap = methodContextMap;
        this.listeners = listeners;
        this.classLoader = new AsmClassLoader();
        if (saveByteCode) {
            this.classLoader.enableSaveByteCode();
        }
    }

    public MethodInvokeDispatcher generate() {
        // 初始类版本、类名、父类
        initClassWrite();
        // 创建构造器
        createConstructor();
        // 创建 doInvoke method
        createDoInvokeMethod();

        return newInstance();
    }

    private void initClassWrite() {
        this.cw.visit(V11, ACC_PUBLIC, CLASS_NAME, null, SUPER_CLASS, null);
    }

    private void createConstructor() {
        MethodVisitor mv = this.cw.visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESC,
                CONSTRUCTOR_SIGN, null);
        mv.visitCode();

        // 调用父类的 super() 构造器
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, SUPER_CLASS, "<init>", CONSTRUCTOR_DESC, false);
        mv.visitInsn(RETURN);

        mv.visitMaxs(-1, -1);
        mv.visitEnd();
    }

    private void createDoInvokeMethod() {
        MethodVisitor mv = this.cw.visitMethod(ACC_PUBLIC, "doInvoke", DO_INVOKE_METHOD_DESC,
                null, DO_INVOKE_METHOD_EX);
        // 通过 DoInvokeMethodAdapter 来进行一些增强操作
        DoInvokeMethodAdapter methodAdapter = new DoInvokeMethodAdapter(ASM9, mv, ACC_PUBLIC,
                "doInvoke", DO_INVOKE_METHOD_DESC);
        methodAdapter.setMethodContextMap(this.methodContextMap);

        methodAdapter.visitCode();
        methodAdapter.visitMaxs(-1, -1);
        methodAdapter.visitEnd();
    }

    private MethodInvokeDispatcher newInstance() {
        Class<?> clazz = loadClass(CLASS_NAME.replace("/", "."), cw);
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(Map.class, List.class);
            constructor.setAccessible(true);
            return (MethodInvokeDispatcher) constructor.newInstance(this.methodContextMap, this.listeners);
        } catch (NoSuchMethodException e) {
            logger.error("obtain constructor failed：{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("instance object failed：{}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public Class<?> loadClass(String className, ClassWriter cw) {
        // 从 ClassWrite 加载类
        this.classLoader.put(className, cw);
        try {
            return this.classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            logger.error("load {} class failed", className);
            e.printStackTrace();
        }

        return null;
    }
}
