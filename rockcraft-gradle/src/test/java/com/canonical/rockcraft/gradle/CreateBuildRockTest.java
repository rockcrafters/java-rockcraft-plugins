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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import com.canonical.rockcraft.builder.IRockcraftNames;

public class CreateBuildRockTest extends BaseRockcraftTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testExport() throws IOException {
        writeString(getBuildFile(), getResource("dependencies-build.in"));
        BuildResult result = runBuild("build-build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        // the task needs to run export dependencies
        Path springBoot = projectDir.toPath().resolve("build/" + IRockcraftNames.BUILD_ROCK_OUTPUT + "/" + IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT + "/org/springframework/boot/spring-boot/2.7.9/spring-boot-2.7.9.jar");
        assertTrue(springBoot.toFile().exists(), "Spring Boot Jar is downloaded");
        Path springBootSha1 = projectDir.toPath().resolve("build/" + IRockcraftNames.BUILD_ROCK_OUTPUT + "/" + IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT + "/org/springframework/boot/spring-boot/2.7.9/spring-boot-2.7.9.jar.sha1");
        String sha1 = new String(Files.readAllBytes(springBootSha1));
        assertEquals("788d60e73e0f7bbbf11b30c3fb0a9cbaa073446b", sha1);
        Path springBootPom = projectDir.toPath().resolve("build/" + IRockcraftNames.BUILD_ROCK_OUTPUT + "/" + IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT + "/org/springframework/boot/spring-boot/2.7.9/spring-boot-2.7.9.pom");
        assertTrue(springBootPom.toFile().exists(), "Spring Boot POM is downloaded");

        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", IRockcraftNames.BUILD_ROCK_OUTPUT, IRockcraftNames.ROCKCRAFT_YAML).toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            String name = (String) parsed.get("name");
            assertEquals("build-" + projectDir.getName(), name);
            Object services = parsed.get("services");
            assertNull(services, "build rock does not define services");

            Map<String, Object> parts = (Map<String, Object>) parsed.get("parts");

            Map<String, Object> buildToolPart =(Map<String, Object>) parts.get("build-tool");
            List<String> buildPackages = (List<String>)buildToolPart.get("build-packages");
            assertTrue(buildPackages.contains("unzip"));
            assertTrue(buildPackages.contains("wget"));
            String buildScript = (String)buildToolPart.get("override-build");
            assertTrue(buildScript.contains("craftctl default"), "default script action present");

            Map<String, Object> dependenciesPart =(Map<String, Object>) parts.get("dependencies");
            buildPackages = (List<String>)dependenciesPart.get("build-packages");
            assertTrue(buildPackages.contains("busybox"));
            assertTrue(buildPackages.contains("openjdk-21-jdk-headless"));
            buildScript = (String)dependenciesPart.get("override-build");
            assertTrue(buildScript.contains("craftctl default"), "default script action present");

            Map<String, Object> cachePart =(Map<String, Object>) parts.get("maven-cache");
            buildScript = (String)cachePart.get("override-build");
            assertTrue(buildScript.contains("${CRAFT_PART_INSTALL}/var/lib/pebble/default/.m2/repository/"));
        }
    }

    @Test
    public void testBuildrockcraftOptions() throws IOException {
        writeString(getBuildFile(), getResource("build-rockcraft-options.in"));
        File buildRock = new File(getProjectDir(), "build-rock");
        buildRock.mkdirs();
        writeString(new File(buildRock, "rockcraft.yaml"), "name: the-rock");
        BuildResult result = runBuild("create-build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", IRockcraftNames.BUILD_ROCK_OUTPUT, IRockcraftNames.ROCKCRAFT_YAML).toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            assertEquals("the-rock",parsed.get("name"));
        }
    }
}
