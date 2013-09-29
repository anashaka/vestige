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

package com.googlecode.vestige.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.googlecode.vestige.core.callable.ClassForName;
import com.googlecode.vestige.core.callable.CreateThread;
import com.googlecode.vestige.core.callable.CreateVestigeClassLoader;
import com.googlecode.vestige.core.callable.InvokeMethod;
import com.googlecode.vestige.core.parser.StringParser;

/**
 * @author Gael Lalire
 */
public final class VestigeExecutor {

    private LinkedList<Runnable> tasks;

    public VestigeExecutor() {
        tasks = new LinkedList<Runnable>();
    }

    public <V> Future<V> submit(final Callable<V> callable) {
        FutureTask<V> futureTask = new FutureTask<V>(callable);
        synchronized (tasks) {
            tasks.addLast(futureTask);
            tasks.notifyAll();
        }
        return futureTask;
    }

    public Object invoke(final ClassLoader contextClassLoader, final Method method, final Object obj, final Object... args)
            throws InterruptedException, IllegalAccessException, InvocationTargetException {
        Future<Object> submit = submit(new InvokeMethod(contextClassLoader, method, obj, args));
        try {
            return submit.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof IllegalAccessException) {
                throw (IllegalAccessException) cause;
            } else if (cause instanceof InvocationTargetException) {
                throw (InvocationTargetException) cause;
            }
            throw new Error("Unknown throwable", cause);
        }
    }

    public <E> VestigeClassLoader<E> createVestigeClassLoader(final ClassLoader parent, final List<List<VestigeClassLoader<?>>> vestigeClassloadersList, final StringParser classStringParser, final StringParser resourceStringParser, final URL... urls) throws InterruptedException {
        Future<VestigeClassLoader<E>> submit = submit(new CreateVestigeClassLoader<E>(parent, vestigeClassloadersList, classStringParser, resourceStringParser, urls));
        try {
            return submit.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new Error("Unknown throwable", cause);
        }
    }

    /**
     * Some class keep stack trace.
     * @throws InterruptedException
     */
    public Class<?> classForName(final ClassLoader loader, final String className) throws ClassNotFoundException,
            InterruptedException {
        Future<Class<?>> submit = submit(new ClassForName(loader, className));
        try {
            return submit.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) cause;
            }
            throw new Error("Unknown throwable", cause);
        }
    }

    private static final LinkedList<FutureTask<Thread>> CREATE_THREADS = new LinkedList<FutureTask<Thread>>();

    private static final Thread WORKER_FACTORY_THREAD;

    static {
        WORKER_FACTORY_THREAD = new Thread("vestige-worker-factory") {
            @Override
            public void run() {
                mainloop: while (true) {
                    Runnable task;
                    synchronized (CREATE_THREADS) {
                        while (CREATE_THREADS.isEmpty()) {
                            try {
                                CREATE_THREADS.wait();
                            } catch (InterruptedException e) {
                                break mainloop;
                            }
                        }
                        task = CREATE_THREADS.removeFirst();
                    }
                    task.run();
                }
            }
        };
        WORKER_FACTORY_THREAD.setDaemon(true);
        WORKER_FACTORY_THREAD.start();
    }

    public Thread createWorker(final String name, final boolean daemon, final int maxActions) throws InterruptedException {
        if (maxActions < 0) {
            throw new IllegalArgumentException("maxActions must be positive integer");
        }
        Runnable runnable;
        if (maxActions != 0) {
            runnable = new Runnable() {
                public void run() {
                    int remain = maxActions;
                    mainloop: while (remain > 0) {
                        Runnable task;
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                try {
                                    tasks.wait();
                                } catch (InterruptedException e) {
                                    break mainloop;
                                }
                            }
                            task = tasks.removeFirst();
                        }
                        task.run();
                        remain--;
                    }
                }
            };
        } else {
            runnable = new Runnable() {
                public void run() {
                    mainloop: while (true) {
                        Runnable task;
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                try {
                                    tasks.wait();
                                } catch (InterruptedException e) {
                                    break mainloop;
                                }
                            }
                            task = tasks.removeFirst();
                        }
                        task.run();
                    }

                }
            };
        }
        FutureTask<Thread> futureTask;
        synchronized (CREATE_THREADS) {
            futureTask = new FutureTask<Thread>(new CreateThread(name, runnable));
            CREATE_THREADS.addLast(futureTask);
            CREATE_THREADS.notifyAll();
        }
        Thread thread;
        try {
            thread = futureTask.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new Error("Unknown throwable", cause);
        }
        thread.setContextClassLoader(null);
        thread.setDaemon(daemon);
        thread.start();
        return thread;
    }

}
