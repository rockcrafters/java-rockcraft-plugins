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

import com.canonical.rockcraft.builder.IRockcraftNames;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateBuildRockTest extends BaseRockcraftTest {

    @Test
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

    }
}
