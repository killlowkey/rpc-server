package com.github.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rpc.core.DefaultRpcRequest;
import com.github.rpc.core.RpcRequest;
import com.github.rpc.utils.RpcUtil;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Ray
 * @date created in 2022/3/5 9:25
 */
@RunWith(JUnit4.class)
public class SerializeTest {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(getObjectValue());
        System.out.println(content);

        Object[] objects = mapper.readValue(content, Object[].class);
        Arrays.stream(objects).forEach(o -> {
            System.out.println(o.getClass());
        });
    }

    @Test
    public void rpcRequestTest() throws Exception {

        Method say = this.getClass().getDeclaredMethod("say", int[].class);
        RpcUtil.registerMethod("say", say);

        RpcRequest rpcRequest = new DefaultRpcRequest("test", "say", new Object[]{new int[]{1,2,3}});

        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(rpcRequest);
        System.out.println(content);
    }

    public void say(int[] arr) {

    }

    public Object[] getObjectValue() {
        return new Object[]{new Integer[]{1, 2, 3, 4}, 1, "hello", new String[]{"hello", "world"}};
    }

    @Data
    @AllArgsConstructor
    static class Person {
        private String name;
        private int age;
    }
}
