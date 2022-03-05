package com.github.rpc.plugins;

import com.github.rpc.invoke.MethodInvokeDispatcher;
import com.github.rpc.invoke.MethodInvokeListener;

/**
 * @author Ray
 * @date created in 2022/3/4 7:16
 */
public abstract class DispatcherProxy implements MethodInvokeDispatcher {

    protected final MethodInvokeDispatcher source;

    protected DispatcherProxy(MethodInvokeDispatcher source) {
        this.source = source;
    }

    @Override
    public void addInvokeListener(MethodInvokeListener listener) {
        if (listener == null) {
            return;
        }

        this.source.addInvokeListener(listener);
    }

    @Override
    public boolean removeInvokeListener(MethodInvokeListener listener) {
        if (listener == null) {
            return false;
        }

        return this.source.removeInvokeListener(listener);
    }
}
