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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that rockcraft plugin can deploy a Spring Boot sample
 */
@Tag("springBoot")
public class SpringBootTest extends BaseRockcraftTest {

    @BeforeEach
    protected void setUp() throws IOException {
        String app = getResource("app.in");
        Path javaDir = Paths.get(projectDir.getAbsolutePath(), "src", "main", "java", "com", "example", "app2");
        assertTrue(javaDir.toFile().mkdirs());
        writeString(javaDir.resolve("DemoApplication.java").toFile(), app);
        String gradle = getResource("spring-boot-project.in");
        writeString(getBuildFile(), gradle);
        writeString(getSettingsFile(), "rootProject.name = 'app2'\n");
    }

    @Test
    public void springBootHelloWorld() throws IOException {
        BuildResult result = runBuild("build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }
}
