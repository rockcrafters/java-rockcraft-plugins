/*
 * Copyright 2024 Canonical Ltd.
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
package com.canonical.rockcraft.builder;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Utilities to build rock image
 */
public class RockBuilder {
    /**
     * Creates RockBuilder
     */
    public RockBuilder() {
    }

    /**
     * Checks that rockcraft is installed
     *
     * @throws InterruptedException - unable to start rockcraft
     * @throws IOException          - unable to start rockcraft
     */
    public static void checkRockcraft() throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder("rockcraft", "--version");
        pb.inheritIO();
        Process versionProcess = pb.start();
        int ret = versionProcess.waitFor();
        if (ret != 0)
            throw new UnsupportedOperationException("Please install rockcraft 'snap install rockcraft'.");
    }

    /**
     * Pushes rock image to the docker
     *
     * @param settings - rockcraft project settings
     * @param options = rockcraft options
     * @throws IOException          - IO error while writing <i>rockcraft.yaml</i>
     * @throws InterruptedException - <i>rockcraft</i> process was aborted
     */
    @SuppressWarnings("unchecked")
    public static void pushRock(RockProjectSettings settings, CommonRockcraftOptions options) throws InterruptedException, IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> rockcraft = yaml.load(new FileReader(settings.getRockOutput().resolve(IRockcraftNames.ROCKCRAFT_YAML).toFile()));
        String imageName = String.valueOf(rockcraft.get(IRockcraftNames.ROCKCRAFT_NAME));
        String imageVersion = String.valueOf(rockcraft.get(IRockcraftNames.ROCKCRAFT_VERSION));
        Path rockDestPath = settings.getRockOutput().resolve(IRockcraftNames.ROCK_OUTPUT);
        for (File file : rockDestPath.toFile().listFiles((dir, file) -> file.endsWith(".rock"))) {
            copyInDocker(file, imageName, imageVersion);
        }
    }

    /**
     * Builds the rock image
     *
     * @param settings - rockcraft project settings
     * @param options - rockcraft options
     * @throws IOException          - IO error while writing <i>rockcraft.yaml</i>
     * @throws InterruptedException - <i>rockcraft</i> process was aborted
     */
    public static void buildRock(RockProjectSettings settings, CommonRockcraftOptions options) throws InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder("rockcraft", "pack")
                .directory(settings.getRockOutput().toFile())
                .inheritIO();
        Process process = pb.start();
        int result = process.waitFor();
        if (result != 0)
            throw new UnsupportedOperationException("Failed to pack rock for " + settings.getName());

        Path rockDestPath = settings.getRockOutput().resolve(IRockcraftNames.ROCK_OUTPUT);
        File rockDest = rockDestPath.toFile();
        rockDest.mkdirs();
        for (File f : rockDest.listFiles((dir, file) -> file.endsWith(".rock"))) {
            f.delete();
        }
        // refresh rocks
        for (File f : settings.getRockOutput().toFile().listFiles((dir, file) -> file.endsWith(".rock"))) {
            Path source = f.toPath();
            Path destination = rockDestPath.resolve(source.getFileName());
            Files.move(source, destination);
        }
    }

    private static void copyInDocker(File ociImage, String imageName, String imageVersion) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("rockcraft.skopeo",
                "copy",
                String.format("oci-archive:%s", ociImage.getAbsolutePath()),
                String.format("docker-daemon:%s:latest", imageName))
                .directory(ociImage.getParentFile())
                .inheritIO();
        Process process = pb.start();
        int result = process.waitFor();
        if (result != 0)
            throw new UnsupportedOperationException("Failed to copy " + ociImage.getAbsolutePath() + " to docker image " + String.format("%s:latest", imageName));

        pb = new ProcessBuilder("docker", "tag", imageName,
                String.format("%s:%s", imageName, imageVersion))
                .directory(ociImage.getParentFile())
                .inheritIO();
        process = pb.start();
        result = process.waitFor();
        if (result != 0)
            throw new UnsupportedOperationException("Failed to tag " + String.format("%s:%s", imageName, imageVersion));
    }
}
