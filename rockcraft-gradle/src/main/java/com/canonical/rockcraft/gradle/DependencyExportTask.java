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
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.StringTokenizer;

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
     * @throws IOException - failed to write project dependencies
     */
    @TaskAction
    public void export() throws IOException {
        for (String configName : dependencyOptions.getConfigurations()) {
            Configuration config = this.getProject().getConfigurations().findByName(configName);
            if (config == null) {
                throw new IllegalArgumentException(String.format("Configuration %s not found.", configName));
            }
            if (!config.isCanBeResolved()) {
                logger.warn(String.format("Configuration %s can not be resolved. skipped.", config.getName()));
                continue;
            }
            copyConfiguration(config, getProject().getDependencies());
        }
        // export build script dependencies
        this.getProject()
                .getBuildscript()
                .getConfigurations()
                .forEach( x ->
                        copyConfiguration(x, getProject().getBuildscript().getDependencies()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void copyConfiguration(Configuration files, DependencyHandler handler) {
        try {
            copyArtifacts(files.getIncoming().getArtifacts());
            // resolve and copy POM files
            Path outputLocationRoot = getOutputDirectory().getAsFile().get().toPath();
            HashSet<ComponentIdentifier> ids = new HashSet<ComponentIdentifier>();
            for (DependencyResult result : files.getIncoming().getResolutionResult().getAllDependencies()) {
                ids.add(result.getFrom().getId());
                logger.debug("Looking up POM for "+ result.getFrom().getId());
            }
            ArtifactResolutionResult artifacts = handler
                    .createArtifactResolutionQuery()
                    .forComponents(ids)
                    .withArtifacts(MavenModule.class, new Class[]{MavenPomArtifact.class})
                    .execute();
            for(ComponentArtifactsResult component : artifacts.getResolvedComponents()) {
                if (component.getId() instanceof ModuleComponentIdentifier) {
                    for(ArtifactResult artifact : component.getArtifacts(MavenPomArtifact.class)) {
                        logger.debug("Found artifact "+ artifact.getId());
                        copyFromGradleCache(((ResolvedArtifactResult)artifact), outputLocationRoot);
                    }
                }
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void copyArtifacts(ArtifactCollection artifacts) throws IOException {
        Path outputLocationRoot = getOutputDirectory().getAsFile().get().toPath();
        for (ResolvedArtifactResult result : artifacts.getArtifacts()) {
            copyFromGradleCache(result, outputLocationRoot);
        }
    }

    private void copyFromGradleCache(ResolvedArtifactResult resolvedArtifact, Path outputLocationRoot ) throws IOException {
        File f = resolvedArtifact.getFile();
        StringTokenizer tk = new StringTokenizer(resolvedArtifact.getId().getComponentIdentifier().getDisplayName(), ":");
        String group = null;
        if (tk.hasMoreTokens()) {
            group = tk.nextToken();
        }
        String name = null;
        if (tk.hasMoreTokens()) {
            name = tk.nextToken();
        }
        String version = null;
        if (tk.hasMoreTokens()) {
            version = tk.nextToken();
        }
        copyFromGradleCache(f, group, name, version, outputLocationRoot);
    }

    private void copyFromGradleCache(File f, String group, String name, String version, Path outputLocationRoot ) throws IOException {
        // gradle cache stores artifacts in <artifact>/<sha1>/<file> directory structure
        File componentLocation = f.getParentFile().getParentFile();
        StringBuilder relativePath = new StringBuilder();
        if (group != null) {
            relativePath.append(group.replace('.', File.separatorChar));
        }
        if (name != null) {
            relativePath.append(File.separatorChar);
            relativePath.append(name);
        }
        if (version != null) {
            relativePath.append(File.separatorChar);
            relativePath.append(version);
        }

        Path outputLocation = outputLocationRoot.resolve(relativePath.toString());
        File[] components = componentLocation.listFiles();
        if (components == null) {
            return;
        }
        for (File component : components) {
            File[] files = component.listFiles();
            if (files == null) {
                continue;
            }
            Optional<File> file = Arrays.stream(files).findFirst();
            if (file.isEmpty()) {
                continue;
            }
            outputLocation.toFile().mkdirs();
            Path output = outputLocation.resolve(file.get().getName());
            Files.copy(file.get().toPath(), output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            Path outputSha1 = Path.of(output.toFile().getAbsolutePath() + ".sha1");
            String paddedSha1 = String.format("%40s", component.getName()).replace(' ', '0');
            Files.writeString(outputSha1, paddedSha1);
            logger.debug(String.format("Written %s and corresponding sha1", output));
        }
    }
}
