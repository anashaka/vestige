package com.googlecode.vestige.core.callable;

import java.net.URL;
import java.util.concurrent.Callable;

import com.googlecode.vestige.core.VestigeClassLoader;

/**
 * @author gaellalire
 */
public class CreateVestigeClassLoader implements Callable<VestigeClassLoader> {

    private ClassLoader parent;

    private URL[] urls;

    public CreateVestigeClassLoader(final ClassLoader parent, final URL[] urls) {
        this.parent = parent;
        this.urls = urls;
    }

    public VestigeClassLoader call() {
        return new VestigeClassLoader(parent, urls);
    }

}
