/**
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
package com.canonical.rockcraft.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SuppressWarnings("unchecked")
@Tag("plugin")
class RockcraftPluginTest extends BaseRockcraftTest {

    @Test
    void buildRockTest() {
        BuildResult result = runBuild("build-rock");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }

    /**
     * Make a separate test for pushing, so that it could be excluded in docker-less
     * scenarios
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    void pushRockTest() throws IOException, InterruptedException {
        BuildResult result = runBuild("push-rock");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        String containerName = projectDir.getName();
        ProcessBuilder pb = new ProcessBuilder("docker", "image", "rm", containerName);
        Process docker = pb.start();
        int wait = docker.waitFor();
        assertEquals(0, wait); // image should be found!
    }

    @Test
    void validRockcraftYaml() throws IOException {
        runBuild("create-rock");
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Object parsedRaw =  yaml.load(is);
            Map<String, Map<String, Object>> parsed = (Map<String, Map<String, Object>>) parsedRaw;
            assertEquals("ubuntu@24.04", ((Map<String, String>)parsedRaw).get("build-base"));
            Map<String, Object> parts = parsed.get("parts");
            //
            Map<String, Object> dumpPart = (Map<String, Object>) parts.get("gradle/rockcraft/dump");
            assertTrue(dumpPart.containsKey("override-build"));
            Map<String, Object> runtimePart = (Map<String, Object>) parts.get("gradle/rockcraft/runtime");
            assertEquals("jlink", runtimePart.get("plugin"));
            Map<String, Object> depsPart = (Map<String, Object>) parts.get("gradle/rockcraft/deps");
            assertTrue(depsPart.containsKey("override-build"));
        }
    }

    @Test
    void buildRockJava11Test() throws IOException {
        writeString(getBuildFile(), getResource("build-rock-java-11.in"));
        BuildResult result = runBuild("build-rock");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }

    @Test
    void rockcraftPluginOptions() throws IOException {
        writeString(getBuildFile(), getResource("rockcraft-plugin-options.in"));
        writeString(new File(getProjectDir(), "readme.txt"), getResource("readme.txt"));
        runBuild("create-rock", "--stacktrace");
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            assertEquals("Foobar", parsed.get("summary"));
        }
    }

    @Test
    void testArchitecture() throws IOException {
        writeString(getBuildFile(), getResource("architecture.in"));
        runBuild("jar", "create-rock");

        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, Object>> parsed = yaml.load(is);
            Map<String, Object> platforms = parsed.get("platforms");
            assertTrue(platforms.containsKey("amd64"));
            assertTrue(platforms.containsKey("arm64"));
        }
    }

    @Test
    void onlySingleRockExists() throws IOException {
        writeString(getBuildFile(), getResource("single-rock1.in"));
        runBuild("build-rock");
        File output = Paths.get(getProjectDir().getAbsolutePath(), "build", "rock").toFile();
        assertEquals(1, output.list((dir, name) -> name.endsWith("rock")).length);

        writeString(getBuildFile(), getResource("single-rock2.in"));
        runBuild("build-rock");
        String[] rocks = output.list((dir, name) -> name.endsWith("rock"));
        assertEquals(1, rocks.length);
        assertTrue(rocks[0].contains("0.02updated"));
    }

    @Test
    void testAllOptions() throws IOException {
        writeString(new File(getProjectDir(), "README.md"), "test");
        writeString(getBuildFile(), getResource("alloptions.in"));
        BuildResult result = runBuild("build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }
}
