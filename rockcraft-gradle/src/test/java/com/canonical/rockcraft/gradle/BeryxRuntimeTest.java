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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("rock")
public class BeryxRuntimeTest extends BaseRockcraftTest {
    protected File getJavaSource() {
        return Paths.get(projectDir.getAbsolutePath(), "src", "main", "java", "beryxtest", "Test.java").toFile();
    }

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        writeString(getJavaSource(), getResource("beryx-test-class.in"));
        writeString(getBuildFile(), getResource("beryx-runtime-project-build.in"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBeryxRuntimeRockcraft() throws IOException {
        BuildResult result = runBuild("create-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            Map<String, Object> services = (Map<String, Object>) parsed.get("services");
            assertTrue(services.containsKey("hello"));
            Map<String, Object> helloService = (Map<String, Object>) services.get("hello");
            assertEquals("/image/bin/hello", helloService.get("command"));

            Map<String, Object> parts = (Map<String, Object>) parsed.get("parts");
            Map<String, Object> dump0 = (Map<String, Object>) parts.get("gradle/rockcraft/dump0");
            String overrideBuild = (String) dump0.get("override-build");
            assertEquals("cp  --archive --link --no-dereference image ${CRAFT_PART_INSTALL}/", overrideBuild);
        }
    }

    /**
     * Integration test - the rock should build successfully
     */
    @Test
    public void testBeryxJlinkBuild() {
        BuildResult result = runBuild("build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }
}
