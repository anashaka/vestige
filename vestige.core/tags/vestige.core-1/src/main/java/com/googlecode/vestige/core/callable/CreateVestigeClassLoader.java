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

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import com.googlecode.vestige.core.VestigeClassLoader;
import com.googlecode.vestige.core.parser.StringParser;

/**
 * @author Gael Lalire
 */
public class CreateVestigeClassLoader implements Callable<VestigeClassLoader> {

    private ClassLoader parent;

    private URL[] urls;

    private List<List<VestigeClassLoader>> vestigeClassloadersList;

    private StringParser classStringParser;

    private StringParser resourceStringParser;

    public CreateVestigeClassLoader(final ClassLoader parent, final List<List<VestigeClassLoader>> vestigeClassloadersList, final StringParser classStringParser, final StringParser resourceStringParser, final URL[] urls) {
        this.parent = parent;
        this.urls = urls;
        this.vestigeClassloadersList = vestigeClassloadersList;
        this.classStringParser = classStringParser;
        this.resourceStringParser = resourceStringParser;
    }

    public VestigeClassLoader call() {
        return new VestigeClassLoader(parent, vestigeClassloadersList, classStringParser, resourceStringParser, urls);
    }

}
