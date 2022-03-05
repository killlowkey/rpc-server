package com.github.rpc.exceptions;

/**
 * @author Ray
 * @date created in 2022/3/3 8:02
 */
public class MethodNotFoundException extends RuntimeException {
    public MethodNotFoundException(String msg) {
        super(msg);
    }
}
