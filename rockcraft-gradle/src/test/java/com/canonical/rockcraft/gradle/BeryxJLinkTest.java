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
public class BeryxJLinkTest extends BaseRockcraftTest {

    protected File getJavaSource() {
        return Paths.get(projectDir.getAbsolutePath(), "src", "main", "java", "beryxtest", "Test.java").toFile();
    }

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        writeString(getJavaSource(), getResource("beryx-test-class.in"));
        writeString(getBuildFile(), getResource("beryx-project-build.in"));
        writeString(Paths.get(projectDir.getAbsolutePath(), "src", "main", "java", "module-info.java").toFile(),
                getResource("beryx-module-info.in"));
    }

    /**
     * Integration test - the rock should build successfully
     *
     * @throws IOException
     */
    @Test
    public void testBeryxJlinkBuild() throws IOException  {
        BuildResult result = runBuild("build-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBeryxJlinkRockcraft() throws IOException {
        BuildResult result = runBuild("create-rock", "--stacktrace");
        assertEquals(TaskOutcome.SUCCESS, getLastTaskOutcome(result)); // the build needs to succeed
        try (FileInputStream is = new FileInputStream(Paths.get(getProjectDir().getAbsolutePath(), "build", "rockcraft.yaml").toFile())) {
            Yaml yaml = new Yaml();
            Map<String, Object> parsed = yaml.load(is);
            Map<String, Object> services = (Map<String, Object>)parsed.get("services");
            assertTrue(services.containsKey("hello"));
            Map<String, Object>  helloService =(Map<String, Object>)services.get("hello");
            assertEquals("/image/bin/hello", helloService.get("command"));

            Map<String, Object> parts = (Map<String, Object>)parsed.get("parts");
            Map<String, Object> dump0 = (Map<String, Object>)parts.get("gradle/rockcraft/dump0");
            String overrideBuild = (String) dump0.get("override-build");
            assertEquals("cp  --archive --link --no-dereference image ${CRAFT_PART_INSTALL}/", overrideBuild);
        }
    }
}
