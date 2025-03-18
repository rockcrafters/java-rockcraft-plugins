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

import com.canonical.rockcraft.builder.DependencyOptions;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * DependencyExportTask writes the project build dependencies to the output
 * directory.
 */
public abstract class DependencyExportTask extends DefaultTask {
    private final Logger logger = Logging.getLogger(DependencyExportTask.class);
    private final ArrayList<ModuleVersionIdentifier> workQueue = new ArrayList<>();
    private final DependencyOptions dependencyOptions;

    /**
     * Constructs DependencyExportTask
     * @param options - dependency export options
     */
    @Inject
    public DependencyExportTask(DependencyOptions options) {
        dependencyOptions = options;
    }

    /**
     * Output directory for the dependency export
     * @return DirectoryProperty
     */
    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Task action to write dependencies
     */
    @TaskAction
    public void export() throws IOException {
        Path outputLocationRoot = getOutputDirectory().getAsFile().get().toPath();
        ArtifactCopy artifactCopy = new ArtifactCopy(outputLocationRoot);


        this.getProject().getConfigurations().all( config -> {
            if (!config.isCanBeResolved()) {
                logger.warn(String.format("Configuration %s can not be resolved. skipped.", config.getName()));
                return;
            }
            PomDependencyReader pomDependencyReader = new PomDependencyReader(getProject().getDependencies(),
                    getProject().getConfigurations(), artifactCopy);
            copyConfiguration(pomDependencyReader, config, getProject().getDependencies(), artifactCopy);
        });

        // export build script dependencies
        final PomDependencyReader pomDependencyReader = new PomDependencyReader(getProject().getBuildscript().getDependencies(),
                getProject().getBuildscript().getConfigurations(), artifactCopy);

        this.getProject()
                .getBuildscript()
                .getConfigurations()
                .forEach( x ->
                        copyConfiguration(pomDependencyReader, x, getProject().getBuildscript().getDependencies(), artifactCopy));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void copyConfiguration(PomDependencyReader pomDependencyReader, Configuration files, DependencyHandler handler, ArtifactCopy artifactCopy) {
        try {

            artifactCopy.copyArtifacts(files.getIncoming().getArtifacts());
            // resolve and copy POM files
            HashSet<ComponentIdentifier> workQueue = new HashSet<ComponentIdentifier>();
            for (ResolvedArtifactResult result : files.getIncoming().getArtifacts()) {
                ComponentIdentifier id = result.getVariant().getOwner();
                workQueue.add(id);
                logger.debug("Looking up POM from Incoming "+ id);
            }
            for (Dependency result : files.getAllDependencies()) {
                if (result.getVersion() != null) {
                    ModuleComponentIdentifier id = DefaultModuleComponentIdentifier.newId(DefaultModuleIdentifier.newId(result.getGroup(), result.getName()), result.getVersion());
                    workQueue.add(id);
                    logger.debug("Looking up POM from AllDeps "+ id);
                }
            }
            Set<String> scopes = new HashSet<>();
            scopes.add("compile");
            scopes.add("import");
            scopes.add("runtime");
            HashSet<ComponentIdentifier> dependencyManagementResolved = new HashSet<>();
            HashSet<ComponentIdentifier> resolved = new HashSet<>();
            while (!workQueue.isEmpty()) {
                ArtifactResolutionResult artifacts = handler
                        .createArtifactResolutionQuery()
                        .forComponents(workQueue)
                        .withArtifacts(MavenModule.class, new Class[]{MavenPomArtifact.class})
                        .execute();
                resolved.addAll(workQueue);
                workQueue.clear();
                for (ComponentArtifactsResult component : artifacts.getResolvedComponents()) {
                    if (component.getId() instanceof ModuleComponentIdentifier) {
                        for (ArtifactResult artifact : component.getArtifacts(MavenPomArtifact.class)) {
                            logger.debug("Found artifact " + artifact.getId());
                            artifactCopy.copyToMavenRepository(((ResolvedArtifactResult) artifact));
                            // resolve maven dependencies to fetch poms
                            DependencyResolutionResult dependencies = pomDependencyReader.read(((ResolvedArtifactResult) artifact).getFile(), scopes);
                            workQueue.addAll(dependencies.dependencies().stream().filter(x -> !resolved.contains(x)).toList());
                            copyBoms(handler,artifactCopy, new HashSet<>(dependencies.dependencyManagement().stream().filter( x -> !dependencyManagementResolved.contains(x)).toList()));
                            dependencyManagementResolved.addAll(dependencies.dependencyManagement());
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void copyBoms(DependencyHandler handler, ArtifactCopy artifactCopy, Set<ComponentIdentifier> componentIdentifiers) throws IOException {
        ArtifactResolutionResult artifacts = handler
                .createArtifactResolutionQuery()
                .forComponents(componentIdentifiers)
                .withArtifacts(MavenModule.class, new Class[]{MavenPomArtifact.class})
                .execute();
        for (ComponentArtifactsResult component : artifacts.getResolvedComponents()) {
            if (component.getId() instanceof ModuleComponentIdentifier) {
                for (ArtifactResult artifact : component.getArtifacts(MavenPomArtifact.class)) {
                    logger.debug("Found artifact " + artifact.getId());
                    artifactCopy.copyToMavenRepository(((ResolvedArtifactResult) artifact));
                }
            }
        }
    }
}
