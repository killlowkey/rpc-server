package com.github.rpc.invoke;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Ray
 * @date created in 2022/3/3 17:25
 */
@Data
@AllArgsConstructor
public class Person {
    private final String name;
    private final int age;
}
