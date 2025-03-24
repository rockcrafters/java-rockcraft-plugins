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

import com.canonical.rockcraft.util.MavenArtifactCopy;

import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.StringTokenizer;

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
        if (group == null || name == null || version == null) {
            logger.warn(String.format("Group, name and version should be set for the artifact %s:%s:%s", group, name, version));
            return;
        }
        copyToMavenRepository(f, group, name, version);
    }
}
