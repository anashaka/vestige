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

package com.googlecode.vestige.platform;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import com.googlecode.vestige.core.parser.StringParser;

/**
 * @author Gael Lalire
 */
public class ClassLoaderConfiguration implements Serializable {

    private static final long serialVersionUID = 4540499333086383595L;

    private Serializable key;

    private URL[] urls;

    private List<ClassLoaderConfiguration> dependencies;

    private List<Integer> paths;

    private List<List<Integer>> pathIdsList;

    private StringParser pathIdsPositionByResourceName;

    private boolean attachmentScoped;

    public ClassLoaderConfiguration(final Serializable key, final boolean attachmentScoped, final URL[] urls, final List<ClassLoaderConfiguration> dependencies, final List<Integer> paths, final List<List<Integer>> pathIdsList, final StringParser pathIdsPositionByResourceName) {
        this.key = key;
        this.attachmentScoped = attachmentScoped;
        this.urls = urls;
        this.dependencies = dependencies;
        this.paths = paths;
        this.pathIdsList = pathIdsList;
        this.pathIdsPositionByResourceName = pathIdsPositionByResourceName;
    }

    public int getDependencyIndex(final int pathIndex) {
        return paths.get(pathIndex * 2).intValue();
    }

    public int getDependencyPathIndex(final int pathIndex) {
        return paths.get(pathIndex * 2 + 1).intValue();
    }

    public Serializable getKey() {
        return key;
    }

    public URL[] getUrls() {
        return urls;
    }

    public List<ClassLoaderConfiguration> getDependencies() {
        return dependencies;
    }

    public List<List<Integer>> getPathIdsList() {
        return pathIdsList;
    }

    public StringParser getPathIdsPositionByResourceName() {
        return pathIdsPositionByResourceName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ClassLoaderConfiguration)) {
            return false;
        }
        ClassLoaderConfiguration other = (ClassLoaderConfiguration) obj;
        return key.equals(other.getKey());
    }

    public boolean isAttachmentScoped() {
        return attachmentScoped;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString();
    }

}
