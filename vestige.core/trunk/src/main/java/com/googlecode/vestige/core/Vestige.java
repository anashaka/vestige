package com.googlecode.vestige.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.googlecode.vestige.core.callable.ClassForName;
import com.googlecode.vestige.core.callable.CreateVestigeClassLoader;
import com.googlecode.vestige.core.callable.InvokeMethod;

/**
 * @author gaellalire
 */
public final class Vestige {

    private static final LinkedList<Runnable> tasks = new LinkedList<Runnable>();

    private static <V> Future<V> submit(final Callable<V> callable) {
        FutureTask<V> futureTask = new FutureTask<V>(callable);
        synchronized (tasks) {
            tasks.addLast(futureTask);
            tasks.notifyAll();
        }
        return futureTask;
    }

    public static Object invoke(final ClassLoader contextClassLoader, final Method method, final Object obj, final Object... args)
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
            throw new Error("Unkown throwable", cause);
        }
    }

    public static VestigeClassLoader createVestigeClassLoader(final ClassLoader parent, final URL... urls)
            throws InterruptedException {
        Future<VestigeClassLoader> submit = submit(new CreateVestigeClassLoader(parent, urls));
        try {
            return submit.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new Error("Unkown throwable", cause);
        }
    }

    /**
     * Some class keep stack trace.
     * @throws InterruptedException
     */
    public static Class<?> classForName(final ClassLoader loader, final String className) throws ClassNotFoundException,
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
            throw new Error("Unkown throwable", cause);
        }
    }

    /**
     * Exit 0 : OK Exit 1 : Bad arguments Exit 2 : URL issue Exit 3 : MainClass
     * issue
     */
    public static void main(final String[] args) {
        if (args.length < 2) {
            System.exit(1);
        }
        String classpath = args[0];
        String mainclass = args[1];
        String[] split = classpath.split(":");
        URL[] urls = new URL[split.length];
        for (int i = 0; i < split.length; i++) {
            try {
                urls[i] = new File(split[i]).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(2);
            }
        }
        String[] dargs = new String[args.length - 2];
        for (int i = 0; i < dargs.length; i++) {
            dargs[i] = args[i + 2];
        }

        createWorker("vestige-worker");

        init(urls, mainclass, dargs);
    }


    private static void init(final URL[] urls, final String mainclass, final String[] dargs) {
        VestigeClassLoader vestigeClassLoader = new VestigeClassLoader(ClassLoader.getSystemClassLoader(), urls);
        vestigeClassLoader.getDelegatedSearchs().add(null);
        Class<?> loadClass = null;
        try {
            loadClass = vestigeClassLoader.loadClass(mainclass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(3);
        }
        Method method = null;
        try {
            method = loadClass.getMethod("main", String[].class);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(3);
        }
        Thread.currentThread().setContextClassLoader(vestigeClassLoader);
        try {
            method.invoke(null, new Object[] {dargs});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.exit(3);
        } finally {
            Thread.currentThread().setContextClassLoader(null);
        }
    }

    public static Thread createWorker(final String name) {
        Thread thread = new Thread(name) {
            @Override
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
        thread.setContextClassLoader(null);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

}
