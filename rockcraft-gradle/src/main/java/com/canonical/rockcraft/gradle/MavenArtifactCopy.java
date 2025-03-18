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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to copy a file into Maven repository
 */
public class MavenArtifactCopy {
    protected final Path outputLocationRoot;

    /**
     * Constructs a new MavenArtifactCopy
     * @outputLocationRoot - output location
     */
    public MavenArtifactCopy(Path outputLocationRoot) {
        this.outputLocationRoot = outputLocationRoot;
    }

    static String computeHash(Path filePath, String alg) throws IOException, NoSuchAlgorithmException {
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

    /**
     * Copy file to the maven repository and write file's sha1
     *
     * @param f       - source file
     * @param group   - maven group id
     * @param name    - maven artifact name
     * @param version - maven artifact version
     * @throws IOException - failed to copy the artifact
     */
    public void copyToMavenRepository(File f, String group, String name, String version) throws IOException {
        Path outputLocation = outputLocationRoot.resolve(String.format("%s/%s/%s", group.replace('.', '/'), name, version));
        outputLocation.toFile().mkdirs();
        Path destinationFile = outputLocation.resolve(f.getName());
        Files.copy(f.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);

        try {
            Path digestFile = Path.of(destinationFile + ".sha1");
            String hash = MavenArtifactCopy.computeHash(destinationFile, "sha1");
            String paddedSha1 = String.format("%40s", hash).replace(' ', '0');
            Files.writeString(digestFile, paddedSha1);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
