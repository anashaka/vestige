package com.googlecode.vestige.core.callable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author gaellalire
 */
public class InvokeMethod implements Callable<Object> {

    private ClassLoader contextClassLoader;

    private Method method;

    private Object obj;

    private Object[] args;

    public InvokeMethod(final ClassLoader contextClassLoader, final Method method, final Object obj, final Object[] args) {
        this.contextClassLoader = contextClassLoader;
        this.method = method;
        this.obj = obj;
        this.args = args;
    }

    public Object call() throws IllegalAccessException, InvocationTargetException {
        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(contextClassLoader);
        try {
            return method.invoke(obj, args);
        } finally {
            currentThread.setContextClassLoader(null);
        }
    }

}
