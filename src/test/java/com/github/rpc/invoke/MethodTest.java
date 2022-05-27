package com.github.rpc.invoke;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Ray
 * @date created in 2022/5/26 19:23
 */
@RunWith(JUnit4.class)
public class MethodTest {

    @Test
    public void test() throws Exception{
        Method method = PersonImpl.class.getDeclaredMethod("say");
        for (Class<?> anInterface : method.getDeclaringClass().getInterfaces()) {
            for (Method m : anInterface.getMethods()) {
                if (method.getName().equals(m.getName())
                        && method.getReturnType().equals(m.getReturnType())
                        && Arrays.equals(method.getParameterTypes(), m.getParameterTypes())) {
                    System.out.println(anInterface.getName());
                }
            }
        }
    }

    interface Person {
        String say();
    }

    class PersonImpl implements Person {
        @Override
        public String say() {
            return null;
        }
    }
}
