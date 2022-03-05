package com.github.rpc.invoke;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 方法上下文
 *
 * @author Ray
 * @date created in 2022/3/3 7:56
 */
@Data
@AllArgsConstructor
public class MethodContext implements Cloneable {
    private Object obj;
    private String name;
    private Method method;

    @Override
    public MethodContext clone() {
        return new MethodContext(this.getObj(), this.name, this.method);
    }
}