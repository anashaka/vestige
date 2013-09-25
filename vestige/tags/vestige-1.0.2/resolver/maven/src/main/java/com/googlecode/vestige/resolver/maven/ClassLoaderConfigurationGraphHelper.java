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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.impl.internal.DependencyModifier;
import org.sonatype.aether.impl.internal.ModifiedDependencyCollector;

/**
 * @author Gael Lalire
 */
public class ClassLoaderConfigurationGraphHelper implements GraphHelper<DependencyNode, MavenArtifact, ClassLoaderConfigurationFactory> {

    private Map<MavenClassLoaderConfigurationKey, ClassLoaderConfigurationFactory> cachedClassLoaderConfigurationFactory;

    private Map<MavenArtifact, URL> urlByKey;

    private ModifiedDependencyCollector modifiedDependencyCollector;

    private CollectRequest collectRequest;

    private MavenRepositorySystemSession session;

    private DependencyModifier dependencyModifier;

    private Map<String, Map<String, MavenArtifact>> runtimeDependencies;

    private Scope scope;

    public ClassLoaderConfigurationGraphHelper(final Map<MavenArtifact, URL> urlByKey,
            final ModifiedDependencyCollector modifiedDependencyCollector, final CollectRequest collectRequest,
            final MavenRepositorySystemSession session, final DependencyModifier dependencyModifier,
            final Map<String, Map<String, MavenArtifact>> runtimeDependencies, final Scope scope) {
        cachedClassLoaderConfigurationFactory = new HashMap<MavenClassLoaderConfigurationKey, ClassLoaderConfigurationFactory>();
        this.urlByKey = urlByKey;
        this.modifiedDependencyCollector = modifiedDependencyCollector;
        this.collectRequest = collectRequest;
        this.session = session;
        this.dependencyModifier = dependencyModifier;
        this.runtimeDependencies = runtimeDependencies;
        this.scope = scope;
    }

    public ClassLoaderConfigurationFactory merge(final List<MavenArtifact> nodes,
            final List<ClassLoaderConfigurationFactory> nexts) {
        List<MavenClassLoaderConfigurationKey> dependencies = new ArrayList<MavenClassLoaderConfigurationKey>();

        for (ClassLoaderConfigurationFactory classLoaderConfiguration : nexts) {
            dependencies.add(classLoaderConfiguration.getKey());
        }

        MavenClassLoaderConfigurationKey key = new MavenClassLoaderConfigurationKey(nodes, dependencies, true);
        ClassLoaderConfigurationFactory classLoaderConfigurationFactory = cachedClassLoaderConfigurationFactory.get(key);
        if (classLoaderConfigurationFactory == null) {
            URL[] urls = new URL[nodes.size()];
            int i = 0;
            for (MavenArtifact artifact : nodes) {
                urls[i] = urlByKey.get(artifact);
                i++;
            }
            classLoaderConfigurationFactory = new ClassLoaderConfigurationFactory(key, scope, urls, nexts);
            cachedClassLoaderConfigurationFactory.put(key, classLoaderConfigurationFactory);
        }
        return classLoaderConfigurationFactory;
    }

    public List<DependencyNode> getNexts(final DependencyNode node) {
        collectRequest.setRoot(node.getDependency());
        DependencyNode collectedNode;
        try {
            collectedNode = modifiedDependencyCollector.collectDependencies(session, collectRequest, dependencyModifier)
                    .getRoot();
        } catch (DependencyCollectionException e) {
            throw new RuntimeException(e);
        }

        return collectedNode.getChildren();
    }

    public MavenArtifact getKey(final DependencyNode node) {
        Artifact artifact = node.getDependency().getArtifact();

        MavenArtifact key = null;
        Map<String, MavenArtifact> map = runtimeDependencies.get(artifact.getGroupId());
        if (map != null) {
            key = map.get(artifact.getArtifactId());
        }
        if (key == null) {
            if (node.getDependency().isOptional()) {
                return null;
            } else {
                throw new RuntimeException(node.getDependency() + " has no classloader conf");
            }
        }

        return key;
    }

}
