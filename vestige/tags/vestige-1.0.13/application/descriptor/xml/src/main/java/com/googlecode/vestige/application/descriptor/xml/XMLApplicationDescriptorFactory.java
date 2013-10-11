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

package com.googlecode.vestige.application.descriptor.xml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.googlecode.vestige.application.ApplicationDescriptor;
import com.googlecode.vestige.application.ApplicationDescriptorFactory;
import com.googlecode.vestige.application.ApplicationException;
import com.googlecode.vestige.application.VersionUtils;
import com.googlecode.vestige.application.descriptor.xml.schema.AddDependency;
import com.googlecode.vestige.application.descriptor.xml.schema.AdditionalRepository;
import com.googlecode.vestige.application.descriptor.xml.schema.Application;
import com.googlecode.vestige.application.descriptor.xml.schema.Config;
import com.googlecode.vestige.application.descriptor.xml.schema.Except;
import com.googlecode.vestige.application.descriptor.xml.schema.MavenConfig;
import com.googlecode.vestige.application.descriptor.xml.schema.ModifyDependency;
import com.googlecode.vestige.application.descriptor.xml.schema.ObjectFactory;
import com.googlecode.vestige.application.descriptor.xml.schema.ReplaceDependency;
import com.googlecode.vestige.resolver.maven.DefaultDependencyModifier;
import com.googlecode.vestige.resolver.maven.MavenArtifactResolver;
import com.googlecode.vestige.resolver.maven.MavenRepository;

/**
 * @author Gael Lalire
 */
public class XMLApplicationDescriptorFactory implements ApplicationDescriptorFactory {

    private MavenArtifactResolver mavenArtifactResolver;

    public XMLApplicationDescriptorFactory(final MavenArtifactResolver mavenArtifactResolver) {
        this.mavenArtifactResolver = mavenArtifactResolver;
    }

    @SuppressWarnings("unchecked")
    public ApplicationDescriptor createApplicationDescriptor(final URL context, final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        URL url;
        try {
            url = new URL(context, appName + "/" + appName + "-" + VersionUtils.toString(version) + ".xml");
        } catch (MalformedURLException e) {
            throw new ApplicationException("url repo issue", e);
        }

        Unmarshaller unMarshaller = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            unMarshaller = jc.createUnmarshaller();

            URL xsdURL = XMLApplicationDescriptorFactory.class.getResource("application.xsd");
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = schemaFactory.newSchema(xsdURL);
            unMarshaller.setSchema(schema);
        } catch (Exception e) {
            throw new ApplicationException("Unable to initialize settings parser", e);
        }
        Application application;
        try {
            application = ((JAXBElement<Application>) unMarshaller.unmarshal(url)).getValue();
        } catch (JAXBException e) {
            throw new ApplicationException("unable to unmarshall application xml", e);
        }

        List<MavenRepository> additionalRepositories = new ArrayList<MavenRepository>();
        DefaultDependencyModifier defaultDependencyModifier = new DefaultDependencyModifier();
        Config configurations = application.getConfigurations();
        if (configurations != null) {
            MavenConfig mavenConfig = configurations.getMavenConfig();
            if (mavenConfig != null) {
                setMavenConfig(configurations.getMavenConfig(), defaultDependencyModifier, additionalRepositories);
            }
        }
        return new XMLApplicationDescriptor(mavenArtifactResolver, repoName + "-" + appName + "-" + VersionUtils.toString(version), version, application, additionalRepositories,
                defaultDependencyModifier);
    }

    public void setMavenConfig(final MavenConfig mavenConfig, final DefaultDependencyModifier defaultDependencyModifier,
            final List<MavenRepository> additionalRepositories) {
        for (Object object : mavenConfig.getModifyDependencyOrReplaceDependencyOrAdditionalRepository()) {
            if (object instanceof ModifyDependency) {
                ModifyDependency modifyDependency = (ModifyDependency) object;
                List<AddDependency> addDependencies = modifyDependency.getAddDependency();
                List<Dependency> dependencies = new ArrayList<Dependency>(addDependencies.size());
                for (AddDependency addDependency : addDependencies) {
                    dependencies.add(new Dependency(new DefaultArtifact(addDependency.getGroupId(),
                            addDependency.getArtifactId(), "jar", addDependency.getVersion()), "runtime"));
                }
                defaultDependencyModifier.add(modifyDependency.getGroupId(), modifyDependency.getArtifactId(), dependencies);
            } else if (object instanceof ReplaceDependency) {
                ReplaceDependency replaceDependency = (ReplaceDependency) object;
                List<AddDependency> addDependencies = replaceDependency.getAddDependency();
                List<Dependency> dependencies = new ArrayList<Dependency>(addDependencies.size());
                for (AddDependency addDependency : addDependencies) {
                    dependencies.add(new Dependency(new DefaultArtifact(addDependency.getGroupId(),
                            addDependency.getArtifactId(), "jar", addDependency.getVersion()), "runtime"));
                }
                Map<String, Set<String>> exceptsMap = null;
                List<Except> excepts = replaceDependency.getExcept();
                if (excepts != null) {
                    exceptsMap = new HashMap<String, Set<String>>();
                    for (Except except : excepts) {
                        Set<String> set = exceptsMap.get(except.getGroupId());
                        if (set == null) {
                            set = new HashSet<String>();
                            exceptsMap.put(except.getGroupId(), set);
                        }
                        set.add(except.getArtifactId());
                    }
                }
                defaultDependencyModifier
                        .replace(replaceDependency.getGroupId(), replaceDependency.getArtifactId(), dependencies, exceptsMap);
            } else if (object instanceof AdditionalRepository) {
                AdditionalRepository additionalRepository = (AdditionalRepository) object;
                additionalRepositories.add(new MavenRepository(additionalRepository.getId(), additionalRepository.getLayout(),
                        additionalRepository.getUrl()));
            }
        }
    }

}
