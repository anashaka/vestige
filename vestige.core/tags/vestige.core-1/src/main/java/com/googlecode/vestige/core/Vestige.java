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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;

import com.googlecode.vestige.core.parser.NoStateStringParser;
import com.googlecode.vestige.core.parser.StringParser;

/**
 * @author Gael Lalire
 */
public final class Vestige {

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("expecting 2 args : classpath and mainClass");
        }
        String classpath = args[0];
        String mainclass = args[1];
        String[] split = classpath.split(File.pathSeparator);
        URL[] urls = new URL[split.length];
        for (int i = 0; i < split.length; i++) {
            urls[i] = new File(split[i]).toURI().toURL();
        }
        String[] dargs = new String[args.length - 2];
        System.arraycopy(args, 2, dargs, 0, dargs.length);

        Class.forName(VestigeExecutor.class.getName(), true, ClassLoader.getSystemClassLoader());

        StringParser stringParser = new NoStateStringParser(0);
        VestigeClassLoader vestigeClassLoader = new VestigeClassLoader(ClassLoader.getSystemClassLoader(),
                Collections.singletonList(Collections.<VestigeClassLoader> singletonList(null)), stringParser, stringParser, urls);
        runMain(vestigeClassLoader, mainclass, dargs);
    }

    public static void runMain(final ClassLoader classLoader, final String mainclass, final String[] dargs) throws Exception {
        Class<?> loadClass = classLoader.loadClass(mainclass);
        Method method = loadClass.getMethod("main", String[].class);
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            method.invoke(null, new Object[] {dargs});
        } finally {
            Thread.currentThread().setContextClassLoader(null);
        }
    }

}
