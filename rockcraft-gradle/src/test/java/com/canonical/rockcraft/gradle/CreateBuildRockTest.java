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
        // export dependencies with implementation and testImplementation dependencies
        writeString(getBuildFile(), getResource("dependencies-build.in"));
        BuildResult result = runBuild("create-build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        Path springBoot = projectDir.toPath().resolve("build/" + IRockcraftNames.BUILD_ROCK_OUTPUT + "/dependencies/org.springframework.boot/spring-boot/3.0.6/spring-boot-3.0.6.jar");
        assertTrue(springBoot.toFile().exists(), "Spring Boot Jar is downloaded");
        Path springBootSha1 = projectDir.toPath().resolve("build/" + IRockcraftNames.BUILD_ROCK_OUTPUT + "/dependencies/org.springframework.boot/spring-boot/3.0.6/spring-boot-3.0.6.jar.sha1");
        String sha1 = Files.readString(springBootSha1);
        assertEquals("095ac2c7aa28fcdef587b2c4f554016f8b9af624", sha1);
    }
}
