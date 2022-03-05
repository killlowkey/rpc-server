package com.github.rpc.invoke.asm;

import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.exceptions.MethodNotFoundException;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import com.github.rpc.invoke.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2022/3/3 16:21
 */
@RunWith(JUnit4.class)
public class AsmGeneratorTest {
    MethodInvokeDispatcher dispatcher;

    @Before
    public void before() {
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner("com.github.rpc.invoke", configuration);
        scanner.scan();

        dispatcher = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.ASM)
                .build();
    }

    @Test
    public void methodInvokeTest() throws Throwable {
        assertEquals("hello, tom", dispatcher.invoke("say", "tom"));
        assertThrows(MethodNotFoundException.class, () -> dispatcher.invoke("exMethod"));
        assertNull(dispatcher.invoke("notReturnValue"));

        assertEquals("tom 10", dispatcher.invoke("hasTwoPrimaryValue", "tom", 10));
        // 没有传方法参数或参数长度不对
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> dispatcher.invoke("hasTwoPrimaryValue"));
        // 方法参数类型错误
        assertThrows(ClassCastException.class, () -> dispatcher.invoke("hasTwoPrimaryValue", 10));

        // array
        assertNull(dispatcher.invoke("arrayArgValue", (Object) new int[]{1, 2, 3}));
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) dispatcher.invoke("arrayReturnValue"));
        assertArrayEquals(new Object[]{"hello", "world", 1, 2}, (Object[]) dispatcher.invoke("array",
                new String[]{"hello", "world"}, new int[]{1, 2}));

        Person person01 = new Person("tom", 10);
        Person person02 = new Person("ray", 10);
        assertNull(dispatcher.invoke("personArray", (Object) new Person[]{person01, person02}));
    }
}