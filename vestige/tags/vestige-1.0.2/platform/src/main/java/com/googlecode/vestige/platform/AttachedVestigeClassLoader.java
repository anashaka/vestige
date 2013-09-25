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

import java.util.List;

import com.googlecode.vestige.core.VestigeClassLoader;

/**
 * @author Gael Lalire
 */
public class AttachedVestigeClassLoader {

    private VestigeClassLoader vestigeClassLoader;

    private List<AttachedVestigeClassLoader> dependencies;

    public AttachedVestigeClassLoader(final VestigeClassLoader vestigeClassLoader, final List<AttachedVestigeClassLoader> dependencies) {
        this.vestigeClassLoader = vestigeClassLoader;
        this.dependencies = dependencies;
    }

    public VestigeClassLoader getVestigeClassLoader() {
        return vestigeClassLoader;
    }

    public List<AttachedVestigeClassLoader> getDependencies() {
        return dependencies;
    }

}
