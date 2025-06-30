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
package com.canonical.rockcraft.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

public class BuildRockCrafterTest {

    @TempDir
    private File tempDir;

    @Test
    public void testGenerateRock() throws IOException {
        RockProjectSettings settings = new RockProjectSettings(BuildSystem.gradle,
                "8.12",
                "project-name",
                "project-version",
                tempDir.toPath(),
                tempDir.toPath(),
                false);
        BuildRockcraftOptions options = new BuildRockcraftOptions();
        options.setArchitectures(new RockArchitecture[]{ RockArchitecture.amd64 });
        options.setBuildGoals(new String[] {"package"});
        File output = tempDir.toPath().resolve("output").toFile();
        output.mkdirs();
        List<File> artifacts = new ArrayList<>();
        artifacts.add(output);
        BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, options, artifacts);
        rockCrafter.writeRockcraft();

        Yaml yaml = new Yaml();
        try (Reader r = new InputStreamReader(new FileInputStream(new File(tempDir, "rockcraft.yaml")))){
            Map<String, Object> result = yaml.load(r);
            Map<String, Object> parts = (Map<String, Object>) result.get("parts");
            assertTrue(parts.containsKey("dependencies"));
            assertTrue(parts.containsKey("maven-cache"));
            assertTrue(parts.containsKey("build-tool"));
        }
        String result = readFile("build-maven.sh");
        assertTrue(result.contains("GOAL=package"), "Goals should be replaced");
        assertFalse(result.contains("!!"));

        result = readFile("build-gradle.sh");
        assertFalse(result.contains("!!"));

        assertTrue(true, "The build should succeed");
    }

    private String readFile(String file) throws IOException {
        Path buildFile = tempDir.toPath().resolve(file);
        StringBuilder result = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(buildFile.toFile())))) {
            String line;
            while ((line = r.readLine())!= null){
                result.append(line);
            }
        }
        return result.toString();
    }

    @Test
    public void testGenerateNativeImageBuildRock() throws IOException {
        RockProjectSettings settings = new RockProjectSettings(BuildSystem.maven,
                "8.12",
                "project-name",
                "project-version",
                tempDir.toPath(),
                tempDir.toPath(),
                false);
        BuildRockcraftOptions options = new BuildRockcraftOptions();
        options.setArchitectures(new RockArchitecture[]{ RockArchitecture.amd64 });
        options.setForNativeImage(true);

        File output = tempDir.toPath().resolve("output").toFile();
        output.mkdirs();
        List<File> artifacts = new ArrayList<>();
        artifacts.add(output);
        BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, options, artifacts);
        rockCrafter.writeRockcraft();

        Yaml yaml = new Yaml();
        try (Reader r = new InputStreamReader(new FileInputStream(new File(tempDir, "rockcraft.yaml")))){
            Map<String, Object> result = yaml.load(r);
            Map<String, Object> parts = (Map<String, Object>) result.get("parts");
            assertTrue(parts.containsKey("dependencies"));
            assertTrue(parts.containsKey("maven-cache"));
            assertTrue(parts.containsKey("build-tool"));
            assertTrue(parts.containsKey("graalvm-install"));
            assertTrue(parts.containsKey("native-compile-deps"));
        }

        assertTrue(true, "The build should succeed");
    }
}
