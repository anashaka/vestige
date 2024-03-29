/*
 * This file is part of Vestige.
 *
 * Vestige is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vestige is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vestige.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.vestige.resolver.maven;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import com.googlecode.vestige.core.parser.StringParser;
import com.googlecode.vestige.platform.ClassLoaderConfiguration;
import com.googlecode.vestige.platform.StringParserFactory;

/**
 * @author Gael Lalire
 */
public class ClassLoaderConfigurationFactory {

    public static final List<Integer> LOCAL_CLASSLOADER_PATH = Collections.singletonList(-1);

    private MavenClassLoaderConfigurationKey classLoaderConfigurationKey;

    private URL[] urls;

    private List<Integer> paths;

    private List<List<Integer>> pathIdsList;

    private List<ClassLoaderConfigurationFactory> factoryDependencies;

    private TreeMap<String, Integer> pathsByResourceName;

    private Scope scope;

    public List<List<Integer>> getPathIdsList() {
        return pathIdsList;
    }

    public TreeMap<String, Integer> getPathsByResourceName() {
        return pathsByResourceName;
    }

    public MavenClassLoaderConfigurationKey getKey() {
        return classLoaderConfigurationKey;
    }

    public URL[] getUrls() {
        return urls;
    }

    public ClassLoaderConfigurationFactory(final MavenClassLoaderConfigurationKey classLoaderConfigurationKey, final Scope scope, final URL[] urls,
            final List<ClassLoaderConfigurationFactory> dependencies) {
        TreeMap<String, List<Integer>> pathsByResourceName = new TreeMap<String, List<Integer>>();
        this.urls = urls;
        this.classLoaderConfigurationKey = classLoaderConfigurationKey;
        this.scope = scope;
        this.factoryDependencies = dependencies;
        paths = new ArrayList<Integer>();
        pathIdsList = new ArrayList<List<Integer>>();

        for (int i = urls.length - 1; i >= 0; i--) {
            URL url = urls[i];
            try {
                JarInputStream openStream = new JarInputStream(url.openStream());
                ZipEntry nextEntry = openStream.getNextEntry();
                while (nextEntry != null) {
                    String name = nextEntry.getName();
                    pathsByResourceName.put(name, LOCAL_CLASSLOADER_PATH);
                    nextEntry = openStream.getNextEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int pos = dependencies.size();
        ListIterator<ClassLoaderConfigurationFactory> it = dependencies.listIterator(pos);
        while (it.hasPrevious()) {
            ClassLoaderConfigurationFactory dependency = it.previous();
            pos--;
            for (Entry<String, Integer> dependencyTmp : dependency.getPathsByResourceName().entrySet()) {
                List<Integer> list = pathsByResourceName.get(dependencyTmp.getKey());
                List<Integer> value = dependency.getPathIdsList().get(dependencyTmp.getValue());
                if (list == null) {
                    list = new ArrayList<Integer>(value.size());
                    pathsByResourceName.put(dependencyTmp.getKey(), list);
                } else if (list == LOCAL_CLASSLOADER_PATH) {
                    list = new ArrayList<Integer>(list);
                    pathsByResourceName.put(dependencyTmp.getKey(), list);
                }
                for (Integer clc : value) {
                    list.add(addPath(pos, clc));
                }
            }
        }

        pathIdsList = new ArrayList<List<Integer>>(new HashSet<List<Integer>>(pathsByResourceName.values()));
        this.pathsByResourceName = new TreeMap<String, Integer>();
        for (Entry<String, List<Integer>> entry : pathsByResourceName.entrySet()) {
            this.pathsByResourceName.put(entry.getKey(), pathIdsList.indexOf(entry.getValue()));
        }
    }

    private int addPath(final int pos, final int path) {
        int ps = paths.size();
        for (int i = 0; i < ps; i += 2) {
            if (paths.get(i) == pos && paths.get(i + 1) == path) {
                return i / 2;
            }
        }
        paths.add(pos);
        paths.add(path);
        return ps / 2;
    }

    private ClassLoaderConfiguration cachedClassLoaderConfiguration;

    public ClassLoaderConfiguration create(final StringParserFactory stringParserFactory) {
        if (cachedClassLoaderConfiguration == null) {
            List<ClassLoaderConfiguration> dependencies = new ArrayList<ClassLoaderConfiguration>(factoryDependencies.size());
            for (ClassLoaderConfigurationFactory classLoaderConfigurationFactory : factoryDependencies) {
                dependencies.add(classLoaderConfigurationFactory.create(stringParserFactory));
            }
            StringParser pathsByResourceName = stringParserFactory.createStringParser(this.pathsByResourceName,
                    0);
            MavenClassLoaderConfigurationKey key;
            if (scope == Scope.PLATFORM) {
                key = classLoaderConfigurationKey;
            } else {
                key = new MavenClassLoaderConfigurationKey(classLoaderConfigurationKey.getArtifacts(), classLoaderConfigurationKey.getDependencies(), false);
            }
            cachedClassLoaderConfiguration = new ClassLoaderConfiguration(key, scope == Scope.ATTACHMENT, urls, dependencies, paths, pathIdsList,
                    pathsByResourceName);
        }
        return cachedClassLoaderConfiguration;
    }

    @Override
    public int hashCode() {
        return classLoaderConfigurationKey.hashCode();
    }

    @Override
    public String toString() {
        return classLoaderConfigurationKey.toString();
    }

}
