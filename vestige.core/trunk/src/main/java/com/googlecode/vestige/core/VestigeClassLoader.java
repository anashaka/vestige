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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.vestige.core.parser.StringParser;

/**
 * @author Gael Lalire
 */
public final class VestigeClassLoader<E> extends URLClassLoader {

    private List<? extends List<? extends VestigeClassLoader<?>>> vestigeClassloadersList;

    private StringParser classStringParser;

    private StringParser resourceStringParser;

    private E data;

    public VestigeClassLoader(final ClassLoader parent, final List<? extends List<? extends VestigeClassLoader<?>>> vestigeClassloadersList,
            final StringParser classStringParser, final StringParser resourceStringParser, final URL... urls) {
        super(urls, parent);
        this.vestigeClassloadersList = vestigeClassloadersList;
        this.classStringParser = classStringParser;
        this.resourceStringParser = resourceStringParser;
    }

    public StringParser getClassStringParser() {
        return classStringParser;
    }

    public StringParser getResourceStringParser() {
        return resourceStringParser;
    }

    public E getData() {
        return data;
    }

    public void setData(final E data) {
        this.data = data;
    }

    public Class<?> superLoadClass(final String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        List<? extends VestigeClassLoader<?>> classLoaders = vestigeClassloadersList.get(classStringParser.match(name));
        if (classLoaders != null) {
            for (VestigeClassLoader<?> classLoader : classLoaders) {
                if (classLoader == null) {
                    try {
                        return super.loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                } else {
                    try {
                        return classLoader.superLoadClass(name);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
        throw new VestigeClassNotFoundException(name, toString());
    }

    public URL superGetResource(final String name) {
        return super.getResource(name);
    }

    @Override
    public URL getResource(final String name) {
        List<? extends VestigeClassLoader<?>> classLoaders = vestigeClassloadersList.get(resourceStringParser.match(name));
        if (classLoaders != null) {
            for (VestigeClassLoader<?> classLoader : classLoaders) {
                if (classLoader == null) {
                    URL resource = super.getResource(name);
                    if (resource != null) {
                        return resource;
                    }
                } else {
                    URL resource = classLoader.superGetResource(name);
                    if (resource != null) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }

    public void superGetResources(final String name, final Set<URL> urls) throws IOException {
        urls.addAll(Collections.list(super.getResources(name)));
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        Set<URL> urls = new LinkedHashSet<URL>();
        List<? extends VestigeClassLoader<?>> classLoaders = vestigeClassloadersList.get(resourceStringParser.match(name));
        if (classLoaders != null) {
            for (VestigeClassLoader<?> classLoader : classLoaders) {
                if (classLoader == null) {
                    urls.addAll(Collections.list(super.getResources(name)));
                } else {
                    classLoader.superGetResources(name, urls);
                }
            }
        }
        return Collections.enumeration(urls);
    }

    @Override
    public String toString() {
        if (data != null) {
            return data.toString();
        }
        return super.toString();
    }

}
