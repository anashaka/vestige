package com.googlecode.vestige.core.callable;

import java.util.concurrent.Callable;

/**
 * @author gaellalire
 */
public class ClassForName implements Callable<Class<?>> {

    private ClassLoader classLoader;

    private String className;

    public ClassForName(final ClassLoader classLoader, final String className) {
        this.classLoader = classLoader;
        this.className = className;
    }

    public Class<?> call() throws ClassNotFoundException {
        return Class.forName(className, true, classLoader);
    }

}
