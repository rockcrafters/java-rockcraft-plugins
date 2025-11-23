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
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseRockcraftTest {

    @TempDir
    protected File projectDir;

    protected File getJavaSource() {
        return Paths.get(projectDir.getAbsolutePath(), "src", "main", "java", "Test.java").toFile();
    }

    protected File getProjectDir() {
        return projectDir;
    }

    protected File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    protected File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    protected void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }

    protected String getResource(String file) throws IOException {
        try (BufferedReader r = new BufferedReader( new InputStreamReader(getClass().getResourceAsStream(file)))) {
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = r.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    TaskOutcome getLastTaskOutcome(BuildResult r){
        if (r.getTasks().isEmpty())
            return null;
        return r.getTasks().get(r.getTasks().size() -1).getOutcome();
    }

    @BeforeEach
    protected void setUp() throws IOException {
        assertTrue(getJavaSource().getParentFile().mkdirs());
        writeString(getJavaSource(), getResource("default-test.in"));
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(), getResource("default-build.in"));
    }

    @AfterEach
    protected void tearDown() throws IOException, InterruptedException {
        new ProcessBuilder("rm", "-rf", projectDir.getAbsolutePath()).start().waitFor();
        new ProcessBuilder("/bin/bash", "-c",
                "lxc list --project rockcraft --format csv -c n | grep -v base-instance | xargs lxc delete -f --project rockcraft")
                .start().waitFor();
    }

    public BuildResult runBuild(String... target) {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments(target);
        runner.withDebug(true);
        runner.withProjectDir(projectDir);
        return runner.build();
    }

}
