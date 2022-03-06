package com.github.rpc.invoke.asm;

import org.objectweb.asm.ClassWriter;
import org.tinylog.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于加载 ClassWrite 生成的类
 *
 * @author Ray
 * @date created in 2022/3/3 12:36
 */
public class AsmClassLoader extends ClassLoader {

    private final Map<String, ClassWriter> classWriterMap = new HashMap<>();
    private boolean save;

    public AsmClassLoader() {

    }

    public AsmClassLoader(String className, ClassWriter cw) {
        this.classWriterMap.put(className, cw);
    }

    public AsmClassLoader(Map<String, ClassWriter> classWriterMap) {
        this.classWriterMap.putAll(classWriterMap);
    }

    public void put(String className, ClassWriter cw) {
        this.classWriterMap.put(className, cw);
    }

    public void enableSaveByteCode() {
        this.save = true;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (this.classWriterMap.containsKey(name)) {
            ClassWriter classWriter = this.classWriterMap.get(name);
            byte[] byteCodeData = classWriter.toByteArray();

            Class<?> result = super.defineClass(name, byteCodeData, 0, byteCodeData.length);
            // 在 debug 模式保存 class 字节码
            if (this.save) {
                try {
                    FileOutputStream fos = new FileOutputStream(result.getSimpleName() + ".class");
                    fos.write(byteCodeData);
                } catch (IOException ex) {
                    Logger.error("save {} class failed，ex：{}",
                            result.getSimpleName(), ex.getMessage());
                }
            }
            return result;
        }

        return super.findClass(name);
    }

}
