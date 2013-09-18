/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.vestige.core.callable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Gael Lalire
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
