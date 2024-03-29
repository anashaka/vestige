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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.graph.Dependency;

/**
 * @author Gael Lalire
 */
public class ReplacementRule {

    private List<Dependency> addedDependencies;

    private Map<String, Set<String>> excepts;

    public ReplacementRule(final List<Dependency> addedDependencies, final Map<String, Set<String>> excepts) {
        this.addedDependencies = addedDependencies;
        this.excepts = excepts;
    }

    public Map<String, Set<String>> getExcepts() {
        return excepts;
    }

    public List<Dependency> getAddedDependencies() {
        return addedDependencies;
    }
}
