package com.github.rpc.invoke;

import com.github.rpc.annotation.RpcService;

import java.util.Arrays;

/**
 * @author Ray
 * @date created in 2022/3/3 10:34
 */
@RpcService
public class MethodExample01 {

    public String say(String name) {
        return "hello, " + name;
    }

    public void notReturnValue() {
    }

    public String hasTwoPrimaryValue(String name, int age) {
        return String.format("%s %d", name, age);
    }

    public void arrayArgValue(int[] value) {
        System.out.println(Arrays.toString(value));
    }

    public int[] arrayReturnValue() {
        return new int[]{1, 2, 3};
    }

    public Object[] array(String[] names, int[] args) {
        Object[] result = new Object[names.length + args.length];
        System.arraycopy(names, 0, result, 0, names.length);
        Integer[] integers = new Integer[args.length];
        for (int i = 0; i < args.length; i++) {
            integers[i] = args[i];
        }
        System.arraycopy(integers, 0, result, names.length, integers.length);
        return result;
    }

    public void personArray(Person[]  people) {
        Arrays.stream(people).forEach(System.out::println);
    }

}
