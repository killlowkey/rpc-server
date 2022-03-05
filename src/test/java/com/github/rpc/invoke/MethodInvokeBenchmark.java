package com.github.rpc.invoke;

import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.core.RpcServiceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Ray
 * @date created in 2022/3/3 17:34
 */
@RunWith(JUnit4.class)
public class MethodInvokeBenchmark {

    int counter = 10_000_000;

    MethodInvokeDispatcher reflect;
    MethodInvokeDispatcher methodHandle;
    MethodInvokeDispatcher asm;

    @Before
    public void setup() {
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner("com.github.rpc.invoke", configuration);
        scanner.scan();

        reflect = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.REFLECT)
                .build();

        methodHandle = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.METHOD_HANDLE)
                .build();

        asm = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.ASM)
                .build();
    }

    @Test
    public void test() throws Throwable {

        execute("ASM " + counter, () -> {
            try {
                asm.invoke("say", "tom");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        execute("Reflect " + counter, () -> {
            try {
                reflect.invoke("say", "tom");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        execute("MethodHandle " + counter, () -> {
            try {
                methodHandle.invoke("say", "tom");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    void execute(String taskName, Runnable runnable) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < counter; i++) {
            runnable.run();
        }
        long end = System.currentTimeMillis();
        System.out.printf("%s 执行耗时 %dms\n", taskName, end - start);
    }
}
