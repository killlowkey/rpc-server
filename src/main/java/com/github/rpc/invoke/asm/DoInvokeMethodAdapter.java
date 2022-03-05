package com.github.rpc.invoke.asm;

import com.github.rpc.invoke.MethodContext;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 关于 ASM 生成字节码一些考虑
 * 1、调用方法参数为原生类型或原生数组参数，需要进行 unbox 操作
 * 2、调用方法返回类型为原生类型或原生数组，需要进行 box 操作
 * 3、需要对 doInvoke 方法中的 obj 参数 进行 checkCast 操作
 * 转换成调用方法所属类，之后在调用其方法
 * 4、doInvoke args 数组元素为引用类型，但与之对应调用方法的原生类型，也需要进行 checkCast 操作
 *      例如：
 *          因为 object02 在传递的时候，会更改为 Object，而参数接收的 List 类型，所以需要进行 cast 操作
 *          Person person = new Person();
 *          doInvoke(person,"say",new Object[]{obj02})
 *          say(List list)
 * 5、引用数组也需要 checkCast：(target[])args[x]
 *
 * @author Ray
 * @date created in 2022/3/3 16:26
 */
public class DoInvokeMethodAdapter extends AdviceAdapter {

    private static final String HELPER_CLASS_NAME = "com/github/rpc/invoke/asm/InvokeHelper";
    private static final String EX_CLASS_NAME = "com/github/rpc/exceptions/MethodNotFoundException";


    private Map<String, MethodContext> methodContextMap;


    public DoInvokeMethodAdapter(int api, MethodVisitor methodVisitor, int access,
                                 String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }

    public void setMethodContextMap(Map<String, MethodContext> methodContextMap) {
        this.methodContextMap = methodContextMap;
    }

    @Override
    protected void onMethodEnter() {
        // 初始化 InvokeHelper
        InvokeHelper.initHelper(this.methodContextMap);
        int size = this.methodContextMap.size();
//        List<Method> methods = InvokeHelper.getMethods();

        // rpc 中没有方法
        if (size == 0) {
            super.visitLdcInsn("no method in rpc service");
            super.visitInsn(ARETURN);
            return;
        }

        Label[] labels = new Label[size];
        Label defaultLabel = new Label();
        for (int i = 0; i < size; i++) {
            labels[i] = new Label();
        }

        // Object;String;Object[] => 0;1;2
        loadArg(1);
        // 获取方法对应的 index
        super.visitMethodInsn(INVOKESTATIC, HELPER_CLASS_NAME, "getMethodIndex",
                "(Ljava/lang/String;)I", false);
        // switch case0 => case method.size()-1
        super.visitTableSwitchInsn(0, size - 1, defaultLabel, labels);

        for (int i = 0; i < size; i++) {
            Method method = InvokeHelper.getMethod(i);
            org.objectweb.asm.commons.Method asmMethod = org.objectweb.asm.commons.Method.getMethod(method);
            // 需要将 . 替换成 /，否则会出现 ClassFormatError 错误
            String type = method.getDeclaringClass().getName().replace(".", "/");

            super.visitLabel(labels[i]);
            // 转换 arg1 为调用方法的所属类的类型
            super.visitVarInsn(ALOAD, 1);
            super.visitTypeInsn(CHECKCAST, type);

            // 加载 Object[] arg3 方法参数
            Type[] argumentTypes = asmMethod.getArgumentTypes();
            // 将 invoke 方法 arg3 参数（Object[]）全部推入到操作栈
            for (int j = 0; j < argumentTypes.length; j++) {
                loadArg(2);
                super.visitIntInsn(BIPUSH, j);
                super.visitInsn(AALOAD);

                Type argumentType = argumentTypes[j];
                String descriptor = argumentType.getDescriptor();
                if (isPrimary(descriptor) || isPrimaryArray(descriptor)) {
                    // 传入的是 Object，需要 unbox 操作
                    // Box type -> unbox type
                    unbox(argumentType);
                } else if (isRef(descriptor)) {
                    // 引用类型传入的是 Object，需要转成具体类型
                    String castType = argumentType.getClassName().replace(".", "/");
                    super.visitTypeInsn(CHECKCAST, castType);
                } else if (isRefArray(descriptor)) {
                    // 引用数组
                    super.visitTypeInsn(CHECKCAST, descriptor);
                }

            }

            // 调用方法
            super.visitMethodInsn(INVOKEVIRTUAL, type, method.getName(), asmMethod.getDescriptor(), false);

            Type returnType = asmMethod.getReturnType();
            String descriptor = returnType.getDescriptor();
            // 该方法没有返回值，那么推入 NULL 到操作栈
            if (descriptor.equals("V")) {
                super.visitInsn(ACONST_NULL);
            }

            // 返回是原生类型，进行 Box 操作，因为方法返回是 Object
            if (isPrimary(descriptor) || isPrimaryArray(descriptor)) {
                box(returnType);
            }

            super.visitInsn(ARETURN);
        }

        // default 实现，抛出 MethodNotFoundException 异常
        super.visitLabel(defaultLabel);
        // 改代码时要修改该异常
        super.visitTypeInsn(NEW, EX_CLASS_NAME);
        super.visitInsn(DUP);
        loadArg(1);
        // String.format("not found %s method", var2)
        super.visitMethodInsn(INVOKESTATIC, HELPER_CLASS_NAME, "formatMsg",
                "(Ljava/lang/String;)Ljava/lang/String;", false);
        super.visitMethodInsn(INVOKESPECIAL, EX_CLASS_NAME, "<init>",
                "(Ljava/lang/String;)V", false);
        super.visitInsn(ATHROW);

    }

    private boolean isPrimary(String desc) {
        String[] descriptors = "BSCIJFD".split("");
        for (String s : descriptors) {
            if (s.equals(desc)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPrimaryArray(String desc) {
        return desc.length() == 2 && desc.charAt(0) == '['
                && isPrimary(String.valueOf(desc.charAt(1)));
    }

    private boolean isRef(String desc) {
        return desc.length() > 2 && desc.charAt(0) == 'L'
                && desc.charAt(desc.length() - 1) == ';';
    }

    private boolean isRefArray(String desc) {
        return desc.length() > 2 && desc.charAt(0) == '['
                && desc.charAt(desc.length() - 1) == ';';
    }
}
