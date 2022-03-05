package com.github.rpc.invoke.mh;

import com.github.rpc.annotation.AnnotationScanner;
import com.github.rpc.core.RpcServiceConfiguration;
import com.github.rpc.exceptions.MethodNotFoundException;
import com.github.rpc.invoke.InvokeType;
import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeDispatcherBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.invoke.WrongMethodTypeException;

import static org.junit.Assert.*;

/**
 * @author Ray
 * @date created in 2022/3/3 10:50
 */
@RunWith(JUnit4.class)
public class MethodHandleMethodInvokeDispatcherTest {

    MethodInvokeDispatcher dispatcher;

    @Before
    public void before() {
        RpcServiceConfiguration configuration = new RpcServiceConfiguration();
        AnnotationScanner scanner = new AnnotationScanner("com.github.rpc.invoke", configuration);
        scanner.scan();

        dispatcher = new MethodInvokeDispatcherBuilder(configuration)
                .invokeType(InvokeType.METHOD_HANDLE)
                .build();
    }

    @Test
    public void methodInvokeTest() throws Throwable {
        assertEquals("hello, tom", dispatcher.invoke("say", "tom"));
        assertThrows(MethodNotFoundException.class, () -> dispatcher.invoke("exMethod"));
        assertNull(dispatcher.invoke("notReturnValue"));

        assertEquals("tom 10", dispatcher.invoke("hasTwoPrimaryValue", "tom", 10));
        assertThrows(WrongMethodTypeException.class, () -> dispatcher.invoke("hasTwoPrimaryValue"));
        assertThrows(WrongMethodTypeException.class, () -> dispatcher.invoke("hasTwoPrimaryValue", 10));

        // array
        assertNull(dispatcher.invoke("arrayArgValue", (Object) new int[]{1, 2, 3}));
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) dispatcher.invoke("arrayReturnValue"));
    }
}