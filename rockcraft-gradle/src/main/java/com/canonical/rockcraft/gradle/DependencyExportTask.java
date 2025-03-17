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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
            PomDependencyReader pomDependencyReader = new PomDependencyReader(getProject().getDependencies(),
                    getProject().getConfigurations());
            copyConfiguration(pomDependencyReader, config, getProject().getDependencies());
        }
        // export build script dependencies
        final PomDependencyReader pomDependencyReader = new PomDependencyReader(getProject().getDependencies(),
                getProject().getConfigurations());

        this.getProject()
                .getBuildscript()
                .getConfigurations()
                .forEach( x ->
                        copyConfiguration(pomDependencyReader, x, getProject().getBuildscript().getDependencies()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void copyConfiguration(PomDependencyReader pomDependencyReader, Configuration files, DependencyHandler handler) {
        try {
            copyArtifacts(files.getIncoming().getArtifacts());
            // resolve and copy POM files
            Path outputLocationRoot = getOutputDirectory().getAsFile().get().toPath();
            HashSet<ComponentIdentifier> workQueue = new HashSet<ComponentIdentifier>();
            for (Dependency result : files.getAllDependencies()) {
                if (result.getVersion() != null) {
                    ModuleComponentIdentifier id = DefaultModuleComponentIdentifier.newId(DefaultModuleIdentifier.newId(result.getGroup(), result.getName()), result.getVersion());
                    workQueue.add(DefaultModuleComponentIdentifier.newId(DefaultModuleIdentifier.newId(result.getGroup(), result.getName()), result.getVersion()));
                    logger.debug("Looking up POM for "+ id);
                }
            }
            HashSet<ComponentIdentifier> resolved = new HashSet<ComponentIdentifier>();
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
                            copyToMavenRepository(((ResolvedArtifactResult) artifact), outputLocationRoot);
                            // resolve maven dependencies to fetch poms
                            Set<ComponentIdentifier> componentIds = pomDependencyReader.read(((ResolvedArtifactResult) artifact).getFile());
                            for (ComponentIdentifier ci : componentIds){
                                if (!resolved.contains(ci)) {
                                    workQueue.add(ci);
                                }
                            }
                        }
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
            copyToMavenRepository(result, outputLocationRoot);
        }
    }

    private void copyToMavenRepository(ResolvedArtifactResult resolvedArtifact, Path outputLocationRoot ) throws IOException {
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
        copyToMavenRepository(f, group, name, version, outputLocationRoot);
    }

    private void copyToMavenRepository(File f, String group, String name, String version, Path outputLocationRoot ) throws IOException {
        if (group == null || name == null || version == null) {
            throw new IllegalArgumentException(String.format("Group, name and version should be set for the artifact %s:%s:%s", group, name, version));
        }
        Path outputLocation = outputLocationRoot.resolve(String.format("%s/%s/%s", group.replace('.', '/'), name, version));
        outputLocation.toFile().mkdirs();
        Path destinationFile = outputLocation.resolve(f.getName());
        Files.copy(f.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

        try {
            Path digestFile = Path.of(destinationFile.toString() + ".sha1");
            String hash = computeHash(destinationFile, "sha1");
            String paddedSha1 = String.format("%40s", hash).replace(' ', '0');
            Files.writeString(digestFile, paddedSha1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        logger.debug(String.format("Written %s and corresponding sha1", destinationFile));
    }

    private static String computeHash(Path filePath, String alg) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(alg);
        byte[] bytes = Files.readAllBytes(filePath);
        digest.update(bytes, 0, bytes.length);
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
