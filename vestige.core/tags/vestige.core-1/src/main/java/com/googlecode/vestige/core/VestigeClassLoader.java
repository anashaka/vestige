package com.googlecode.vestige.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gaellalire
 */
public final class VestigeClassLoader extends URLClassLoader {

    private List<DelegatedSearch> delegatedSearchs = new ArrayList<DelegatedSearch>();

    private Map<String, String> properties = new HashMap<String, String>();

    private Set<VestigeClassLoader> excludes = new HashSet<VestigeClassLoader>();

    public VestigeClassLoader(final ClassLoader parent, final URL... urls) {
        super(urls, parent);
    }

    public Set<VestigeClassLoader> getExcludes() {
        return excludes;
    }

    public String getProperty(final String name) {
        return properties.get(name);
    }

    public void setProperty(final String name, final String value) {
        properties.put(name, value);
    }

    public List<DelegatedSearch> getDelegatedSearchs() {
        return delegatedSearchs;
    }

    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        return loadExcluding(name, excludes);
    }

    @Override
    public URL getResource(final String name) {
        return getResourceExcluding(name, excludes);
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        return Collections.enumeration(getResourcesExcluding(name, excludes));
    }

    public Class<?> loadExcluding(final String name, final Set<VestigeClassLoader> excludes) throws ClassNotFoundException {
        boolean superLoadFailed = false;
        boolean canSuperLoad = !excludes.contains(this) && !this.excludes.contains(this);
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (canSuperLoad && !superLoadFailed) {
                    try {
                        Class<?> loadClass = super.loadClass(name);
                        if (!excludes.contains(loadClass.getClassLoader())) {
                            return loadClass;
                        }
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                    superLoadFailed = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getClassPattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (canSuperLoad && !superLoadFailed) {
                    try {
                        Class<?> loadClass = super.loadClass(name);
                        if (!excludes.contains(loadClass.getClassLoader())) {
                            return loadClass;
                        }
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                    superLoadFailed = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    try {
                        return classLoader.loadExcluding(name, excludes);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
        throw new VestigeClassNotFoundException(name, properties);
    }

    /**
     * This method do not try to get resource from delegate, which prevent getting resource from an excluded classloader.
     */
    public URL superGetResource(final String name) {
        ClassLoader parent = getParent();
        URL resource;
        if (parent == null) {
            // ok if the class path do not contains resources
            resource = ClassLoader.getSystemResource(name);
        } else {
            resource = parent.getResource(name);
        }
        if (resource != null) {
            return resource;
        }
        return super.findResource(name);
    }

    public Set<URL> superGetResources(final String name) throws IOException {
        Set<URL> urls = new HashSet<URL>();
        ClassLoader parent = getParent();
        if (parent == null) {
            // ok if the class path do not contains resources
            urls.addAll(Collections.list(ClassLoader.getSystemResources(name)));
        } else {
            urls.addAll(Collections.list(parent.getResources(name)));
        }
        urls.addAll(Collections.list(super.findResources(name)));
        return urls;
    }


    public URL getResourceExcluding(final String name, final Set<VestigeClassLoader> excludes) {
        boolean superGetFailed = false;
        boolean canSuperGet = !excludes.contains(this) && !this.excludes.contains(this);
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (canSuperGet && !superGetFailed) {
                    URL resource = superGetResource(name);
                    if (resource != null) {
                        return resource;
                    }
                    superGetFailed = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getResourcePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (canSuperGet && !superGetFailed) {
                    URL resource = superGetResource(name);
                    if (resource != null) {
                        return resource;
                    }
                    superGetFailed = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    URL resource = classLoader.getResourceExcluding(name, excludes);
                    if (resource != null) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }

    public Set<URL> getResourcesExcluding(final String name, final Set<VestigeClassLoader> excludes) throws IOException {
        Set<URL> urls = new HashSet<URL>();
        boolean superGetDone = false;
        boolean canSuperGet = !excludes.contains(this) && !this.excludes.contains(this);
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (canSuperGet && !superGetDone) {
                    urls.addAll(superGetResources(name));
                    superGetDone = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getResourcePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (canSuperGet && !superGetDone) {
                    urls.addAll(superGetResources(name));
                    superGetDone = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    urls.addAll(classLoader.getResourcesExcluding(name, excludes));
                }
            }
        }
        return urls;
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        boolean superFindFailed = false;
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (!superFindFailed) {
                    try {
                        return super.findClass(name);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                    superFindFailed = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getClassPattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (!superFindFailed) {
                    try {
                        return super.findClass(name);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                    superFindFailed = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    try {
                        return classLoader.loadExcluding(name, excludes);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
        throw new VestigeClassNotFoundException(name, properties);
    }

    @Override
    public URL findResource(final String name) {
        boolean superFindFailed = false;
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (!superFindFailed) {
                    URL resource = super.findResource(name);
                    if (resource != null) {
                        return resource;
                    }
                    superFindFailed = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getResourcePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (!superFindFailed) {
                    URL resource = super.findResource(name);
                    if (resource != null) {
                        return resource;
                    }
                    superFindFailed = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    URL resource = classLoader.getResourceExcluding(name, excludes);
                    if (resource != null) {
                        return resource;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        Set<URL> urls = new HashSet<URL>();
        boolean superFindDone = false;
        for (DelegatedSearch classSearch : delegatedSearchs) {
            if (classSearch == null) {
                if (!superFindDone) {
                    urls.addAll(Collections.list(super.findResources(name)));
                    superFindDone = true;
                }
                continue;
            }
            Pattern pattern = classSearch.getResourcePattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    continue;
                }
            }
            // pattern is null or match
            List<VestigeClassLoader> classLoaders = classSearch.getClassLoaders();
            if (classLoaders == null) {
                if (!superFindDone) {
                    urls.addAll(Collections.list(super.findResources(name)));
                    superFindDone = true;
                }
            } else {
                for (VestigeClassLoader classLoader : classLoaders) {
                    urls.addAll(classLoader.getResourcesExcluding(name, excludes));
                }
            }
        }
        return Collections.enumeration(urls);
    }

}
