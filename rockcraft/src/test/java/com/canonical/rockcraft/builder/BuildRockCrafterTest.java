/**
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BuildRockCrafterTest {

    @TempDir
    private File tempDir;

    @Test
    public void testGenerateRock() throws IOException, InterruptedException {
        RockProjectSettings settings = new RockProjectSettings(Generator.gradle,
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
        RockBuilder.buildRock(settings, null);
        assertTrue(true, "The build should succeed");
    }
}
