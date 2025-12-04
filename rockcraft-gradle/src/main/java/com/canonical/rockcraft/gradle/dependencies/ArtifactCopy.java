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
package com.canonical.rockcraft.gradle.dependencies;

import com.canonical.rockcraft.util.MavenArtifactCopy;

import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Utility class to copy artifact to the output location
 */
public class ArtifactCopy extends MavenArtifactCopy {
    private final Logger logger = Logging.getLogger(ArtifactCopy.class);

    /**
     * Construct ArtifactCopy
     * @param outputLocationRoot - destination for the artifacts
     */
    public ArtifactCopy(Path outputLocationRoot) {
        super(outputLocationRoot);
    }

    /**
     * Copy the artifact collection
     * @param artifacts - artifact collection as returned by gradle resolution
     * @throws IOException - failed to copy the collection
     */
    public void copyArtifacts(ArtifactCollection artifacts) throws IOException {
        for (ResolvedArtifactResult result : artifacts.getArtifacts()) {
            copyToMavenRepository(result);
        }
    }

    /**
     * Copy individual artifact to the destination
     * @param resolvedArtifact - resolved artifact
     * @throws IOException - failed to copy the artifact
     */
    public void copyToMavenRepository(ResolvedArtifactResult resolvedArtifact) throws IOException {
        File f = resolvedArtifact.getFile();
        String[] split = resolvedArtifact.getId().getComponentIdentifier().getDisplayName().split(":");
        String group = split.length > 0 ? split[0] : null;
        String name = split.length > 1 ? split[1] : null;
        String version = split.length > 2 ? split[2] : null;
        if (group == null || name == null || version == null) {
            logger.warn("Group, name and version should be set for the artifact {}:{}:{}", group, name, version);
            return;
        }
        copyToMavenRepository(f, group, name, version);
    }

    public void writeCompanionJar(ResolvedArtifactResult resolvedArtifact) throws IOException {
        File f = resolvedArtifact.getFile();
        if (!f.getName().endsWith(".pom")) {
            return;
        }
        String[] split = resolvedArtifact.getId().getComponentIdentifier().getDisplayName().split(":");
        String group = split.length > 0 ? split[0] : null;
        String name = split.length > 1 ? split[1] : null;
        String version = split.length > 2 ? split[2] : null;
        if (group == null || name == null || version == null) {
            logger.warn("Group, name and version should be set for the artifact {}:{}:{}", group, name, version);
            return;
        }
        Path outputLocation = getDestinationPath(group, name, version);
        String jarName = f.getName().substring(0, f.getName().length() - ".pom".length()) + ".jar";
        File jarFile = outputLocation.resolve(jarName).toFile();
        if (jarFile.exists()) {
            return;
        }
        createCompanionJar(jarFile);
        writeDigest(jarFile.toPath());
    }

    private void createCompanionJar(File f) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_TITLE, "Empty Jar");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0.0");

        try (FileOutputStream fos = new FileOutputStream(f);
             JarOutputStream target = new JarOutputStream(fos, manifest)) {
            JarEntry entry = new JarEntry("readme.txt");
            entry.setTime(System.currentTimeMillis());
            target.putNextEntry(entry);
            target.write("This is a placeholder jar file".getBytes());
            target.closeEntry();
        }
    }
}
