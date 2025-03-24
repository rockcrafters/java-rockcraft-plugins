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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

public class BuildRockCrafterTest {

    @TempDir
    private File tempDir;

    @Test
    public void testGenerateRock() throws IOException, InterruptedException {
        RockProjectSettings settings = new RockProjectSettings(BuildSystem.gradle,
                "8.12",
                "project-name",
                "project-version",
                tempDir.toPath(),
                tempDir.toPath(),
                false);
        BuildRockcraftOptions options = new BuildRockcraftOptions();
        options.setArchitectures(new RockArchitecture[]{ RockArchitecture.amd64 });

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

        assertTrue(true, "The build should succeed");
    }
}
