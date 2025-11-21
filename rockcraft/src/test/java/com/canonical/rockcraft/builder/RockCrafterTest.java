package com.canonical.rockcraft.builder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class RockCrafterTest {
    @TempDir
    private File tempDir;

    @SuppressWarnings("unchecked")
    @Test
    public void testCommonSection() throws IOException {
        RockProjectSettings settings = new RockProjectSettings(BuildSystem.maven,
                "8.12",
                "project-name",
                "project-version",
                tempDir.toPath(),
                tempDir.toPath(),
                false);
        RockcraftOptions options = new RockcraftOptions();
        File output = tempDir.toPath().resolve("output").toFile();
        output.mkdirs();
        List<File> artifacts = new ArrayList<>();
        artifacts.add(output);
        RockCrafter rockCrafter = new RockCrafter(settings, options, artifacts);
        rockCrafter.writeRockcraft();
        Yaml yaml = new Yaml();
        try (Reader r = new InputStreamReader(new FileInputStream(new File(tempDir, "rockcraft.yaml")))){
            Map<String, Object> result = yaml.load(r);
            assertEquals("Please set summary for your rock", result.get("summary"));
            assertEquals("Please set description for your rock", result.get("description"));
            assertEquals("project-name", result.get(IRockcraftNames.ROCKCRAFT_NAME));
            assertEquals("project-version", result.get(IRockcraftNames.ROCKCRAFT_VERSION));
        }
    }
}
