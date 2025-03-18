/*
 * Copyright 2025 Canonical Ltd.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.canonical.rockcraft.gradle;

import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("deprecation")
class PomResolver implements ModelResolver {
    private final ConfigurationContainer configurationContainer;
    private final DependencyHandler handler;
    private final ArtifactCopy artifactCopy;

    public PomResolver(DependencyHandler handler, ConfigurationContainer configurationContainer, ArtifactCopy artifactCopy) {
        this.configurationContainer = configurationContainer;
        this.handler = handler;
        this.artifactCopy = artifactCopy;
    }

    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
        Dependency dep = handler.create(String.format("%s:%s:%s@pom", groupId, artifactId, version));
        Configuration pomConfiguration = configurationContainer.detachedConfiguration(dep);
        final File pomXml = pomConfiguration.getSingleFile();
        try {
            artifactCopy.copyToMavenRepository(pomXml, groupId, artifactId, version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ModelSource() {
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(pomXml);
            }

            public String getLocation() {
                return pomXml.getAbsolutePath();
            }
        };
    }

    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return this.resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    public ModelSource resolveModel(org.apache.maven.model.Dependency dependency) throws UnresolvableModelException {
        return this.resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    public void addRepository(Repository repository) throws InvalidRepositoryException {
        this.addRepository(repository, false);
    }

    public void addRepository(final Repository repository, boolean replace) throws InvalidRepositoryException {
        // do nothing
    }

    public ModelResolver newCopy() {
        return this;
    }
}
