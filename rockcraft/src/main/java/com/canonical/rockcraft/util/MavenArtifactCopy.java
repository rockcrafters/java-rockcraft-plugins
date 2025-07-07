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
package com.canonical.rockcraft.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to copy a file into Maven repository
 */
public class MavenArtifactCopy {
    /**
     * Output location for the copy operation
     */
    protected final Path outputLocationRoot;

    /**
     * Constructs a new MavenArtifactCopy
     * @param outputLocationRoot - output location
     */
    public MavenArtifactCopy(Path outputLocationRoot) {
        this.outputLocationRoot = outputLocationRoot;
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

    private Path getDestinationPath(String group, String name, String version) {
        return outputLocationRoot.resolve(String.format("%s/%s/%s", group.replace('.', '/'), name, version));
    }

    /**
     * Copy file to the maven repository and write file's sha1
     * synchronized to avoid writing the same file from multiple resolvers
     * @param f       - source file
     * @param group   - maven group id
     * @param name    - maven artifact name
     * @param version - maven artifact version
     * @throws IOException - failed to copy the artifact
     */
    public synchronized void copyToMavenRepository(File f, String group, String name, String version) throws IOException {
        Path outputLocation = getDestinationPath(group, name, version);
        outputLocation.toFile().mkdirs();
        Path destinationFile = outputLocation.resolve(f.getName());
        Files.copy(f.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        writeDigest(destinationFile);
    }

    private static void writeDigest(Path destinationFile) throws IOException {
        // do not checksum checksums
        if (destinationFile.toString().endsWith(".sha1")) {
            return;
        }
        try {
            Path digestFile = Paths.get(destinationFile + ".sha1");
            String hash = MavenArtifactCopy.computeHash(destinationFile, "sha1");
            String paddedSha1 = String.format("%40s", hash).replace(' ', '0');
            Files.write(digestFile, paddedSha1.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
