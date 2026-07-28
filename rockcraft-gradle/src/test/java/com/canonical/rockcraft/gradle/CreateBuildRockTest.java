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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import com.canonical.rockcraft.builder.IRockcraftNames;

@Tag("buildRock")
public class CreateBuildRockTest extends BaseRockcraftTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testExport() throws IOException {
        writeString(getBuildFile(), getResource("dependencies-build.in"));
        BuildResult result = runBuild(ITaskNames.BUILD_BUILD_ROCK, "--stacktrace");
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
            assertNull(buildPackages);
            buildScript = (String)dependenciesPart.get("override-build");
            assertTrue(buildScript.contains("craftctl default"), "default script action present");

            Map<String, Object> mavenRepoPart =(Map<String, Object>) parts.get("maven-cache");
            buildScript = (String)mavenRepoPart.get("override-build");
            assertTrue(buildScript.contains("${CRAFT_PART_INSTALL}/home/ubuntu/.m2"));
        }
    }

    @Test
    public void testBuildrockcraftOptions() throws IOException {
        writeString(getBuildFile(), getResource("build-rockcraft-options.in"));
        File buildRock = new File(getProjectDir(), IRockcraftNames.BUILD_ROCK_OUTPUT);
        buildRock.mkdirs();
        writeString(new File(buildRock, "rockcraft.yaml"), "name: the-rock");
        BuildResult result = runBuild(ITaskNames.CREATE_BUILD_ROCK, "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", IRockcraftNames.BUILD_ROCK_OUTPUT, IRockcraftNames.ROCKCRAFT_YAML).toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            assertEquals("the-rock",parsed.get("name"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBuildRockcraftPartMerge() throws IOException {
        writeString(getBuildFile(), getResource("build-rockcraft-options.in"));
        File buildRock = new File(getProjectDir(), IRockcraftNames.BUILD_ROCK_OUTPUT);
        buildRock.mkdirs();
        writeString(new File(buildRock, "rockcraft.yaml"), getResource("build-rockcraft.in"));
        BuildResult result = runBuild(ITaskNames.CREATE_BUILD_ROCK, "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", IRockcraftNames.BUILD_ROCK_OUTPUT, IRockcraftNames.ROCKCRAFT_YAML).toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            Map<String, Object> parts = (Map<String, Object>) parsed.get("parts");
            Map<String, Object> buildTool = (Map<String, Object>) parts.get("build-tool");
            // plugin is overriden
            assertEquals("foo", buildTool.get("plugin"));
            // override build tag is not changed
            assertTrue(String.valueOf(buildTool.get("override-build")).contains("wget"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToolchainOption() throws IOException {
        writeString(getBuildFile(), getResource("toolchain-options.in"));
        BuildResult result = runBuild(ITaskNames.CREATE_BUILD_ROCK, "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", IRockcraftNames.BUILD_ROCK_OUTPUT, IRockcraftNames.ROCKCRAFT_YAML).toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            Map<String, Object> parts = (Map<String, Object>) parsed.get("parts");
            Map<String, Object> openjdk = (Map<String, Object>) parts.get("openjdk");
            List<String> packages = (List<String>)openjdk.get("build-packages");
            // toolchain is specified, so the build uses openjdk-17
            assertEquals(1, packages.size());
            assertEquals("openjdk-17-jdk-headless", packages.get(0));
        }
    }

    @Test
    public void testBuildBuildRockSkippedWithoutRockcraftProperty() throws IOException {
        writeString(getBuildFile(), getResource("build-build-rock-no-rockcraft.in"));
        BuildResult result = runBuild(ITaskNames.BUILD_BUILD_ROCK, "--stacktrace");
        assertEquals(TaskOutcome.SKIPPED, result.task(":" + ITaskNames.BUILD_BUILD_ROCK).getOutcome());
    }

    @Test
    public void testLocalBuildGradleInitScript() throws IOException {
        // Create settings.gradle with pluginManagement repositories
        writeString(getSettingsFile(),
            "pluginManagement {\n" +
            "    repositories {\n" +
            "        mavenCentral()\n" +
            "        gradlePluginPortal()\n" +
            "    }\n" +
            "}\n" +
            "rootProject.name = 'test-project'\n" +
            "include 'subproject'\n");

        // Create root build.gradle with repositories and buildscript repositories
        writeString(getBuildFile(),
            "buildscript {\n" +
            "    repositories {\n" +
            "        mavenCentral()\n" +
            "        google()\n" +
            "    }\n" +
            "}\n" +
            "repositories {\n" +
            "    mavenCentral()\n" +
            "    maven { url = uri('https://repo.spring.io/milestone') }\n" +
            "}\n" +
            "\n" +
            "task dumpRepositories {\n" +
            "    doLast {\n" +
            "        println '=== Root Project Repositories ==='\n" +
            "        repositories.each { repo ->\n" +
            "            if (repo instanceof MavenArtifactRepository) {\n" +
            "                println \"REPO:${repo.url}\"\n" +
            "            }\n" +
            "        }\n" +
            "        println '=== Root Buildscript Repositories ==='\n" +
            "        buildscript.repositories.each { repo ->\n" +
            "            if (repo instanceof MavenArtifactRepository) {\n" +
            "                println \"BUILDSCRIPT_REPO:${repo.url}\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

        // Create subproject directory and build file
        File subprojectDir = new File(projectDir, "subproject");
        subprojectDir.mkdirs();
        writeString(new File(subprojectDir, "build.gradle"),
            "buildscript {\n" +
            "    repositories {\n" +
            "        mavenCentral()\n" +
            "        maven { url = uri('https://repo.gradle.org/gradle/libs-releases') }\n" +
            "    }\n" +
            "}\n" +
            "repositories {\n" +
            "    mavenCentral()\n" +
            "    maven { url = uri('https://plugins.gradle.org/m2/') }\n" +
            "}\n" +
            "\n" +
            "task dumpRepositories {\n" +
            "    doLast {\n" +
            "        println '=== Subproject Repositories ==='\n" +
            "        repositories.each { repo ->\n" +
            "            if (repo instanceof MavenArtifactRepository) {\n" +
            "                println \"SUBPROJECT_REPO:${repo.url}\"\n" +
            "            }\n" +
            "        }\n" +
            "        println '=== Subproject Buildscript Repositories ==='\n" +
            "        buildscript.repositories.each { repo ->\n" +
            "            if (repo instanceof MavenArtifactRepository) {\n" +
            "                println \"SUBPROJECT_BUILDSCRIPT_REPO:${repo.url}\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

        Path initScript = Paths.get("../rockcraft/src/main/resources/com/canonical/rockcraft/builder/local-build.gradle");
        // Run build with init script
        BuildResult result = runBuild("--init-script",
                initScript.toAbsolutePath().toString(), "dumpRepositories", ":subproject:dumpRepositories");
        assertEquals(TaskOutcome.SUCCESS, result.task(":dumpRepositories").getOutcome());
        assertEquals(TaskOutcome.SUCCESS, result.task(":subproject:dumpRepositories").getOutcome());

        String output = result.getOutput();
        // Gradle normalizes file:/// to file:/ (single slash)
        String expectedRepo = "file:/home/ubuntu/.m2/repository";

        // Verify all repositories point to local repo
        // Root project repositories
        assertTrue(output.contains("REPO:" + expectedRepo),
            "Root project repository should point to local repo. Output: " + output);
        // Root buildscript repositories
        assertTrue(output.contains("BUILDSCRIPT_REPO:" + expectedRepo),
            "Root buildscript repository should point to local repo. Output: " + output);
        // Subproject repositories
        assertTrue(output.contains("SUBPROJECT_REPO:" + expectedRepo),
            "Subproject repository should point to local repo. Output: " + output);
        // Subproject buildscript repositories
        assertTrue(output.contains("SUBPROJECT_BUILDSCRIPT_REPO:" + expectedRepo),
            "Subproject buildscript repository should point to local repo. Output: " + output);
    }
}
