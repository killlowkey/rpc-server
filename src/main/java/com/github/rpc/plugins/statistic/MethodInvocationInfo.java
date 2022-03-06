package com.github.rpc.plugins.statistic;

import lombok.Data;

import java.util.Date;

/**
 * 方法调用信息
 *
 * @author Ray
 * @date created in 2022/3/6 21:49
 */
@Data
public class MethodInvocationInfo {

    private String name;
    private Object[] args;
    private Object result;
    private Throwable ex;
    private Date start;
    private Date end;

    public MethodInvocationInfo(String name, Object... args) {
        this.name = name;
        this.args = args;
        this.start = new Date();
    }
}
