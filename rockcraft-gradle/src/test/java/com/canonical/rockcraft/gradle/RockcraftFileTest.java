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
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("rock")
public class RockcraftFileTest extends BaseRockcraftTest {

    protected File getRockcraftFile() {
        return new File(projectDir, "rockcraft.yaml");
    }

    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        writeString(getBuildFile(), getResource("default-build-with-rockcraft.in"));
        writeString(getRockcraftFile(), getResource("rockcraft.in"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateRockWithoutService() throws IOException {
        BuildResult result = runBuild("create-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            assertEquals("ubuntu@24.04", parsed.get("base"));
            Map<String, Object> parts = (Map<String, Object>)parsed.get("parts");
            Map<String, Object> dump =  (Map<String, Object>)parts.get("gradle/rockcraft/dump");
            assertEquals("echo ok", dump.get("override-build"));
            Map<String, Object> services = (Map<String, Object>)parsed.get("services");
            assertNull(services);
        }
        result = runBuild("build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }
}
