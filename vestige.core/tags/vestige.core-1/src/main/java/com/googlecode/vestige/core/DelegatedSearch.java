package com.googlecode.vestige.core;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author gaellalire
 */
public final class DelegatedSearch {

    private Pattern classPattern;

    private Pattern resourcePattern;

    private List<VestigeClassLoader> classLoaders;

    public DelegatedSearch(final Pattern classPattern, final Pattern resourcePattern, final List<VestigeClassLoader> classLoaders) {
        this.classPattern = classPattern;
        this.resourcePattern = resourcePattern;
        this.classLoaders = classLoaders;
    }

    public Pattern getClassPattern() {
        return classPattern;
    }

    public Pattern getResourcePattern() {
        return resourcePattern;
    }

    public List<VestigeClassLoader> getClassLoaders() {
        return classLoaders;
    }

}
