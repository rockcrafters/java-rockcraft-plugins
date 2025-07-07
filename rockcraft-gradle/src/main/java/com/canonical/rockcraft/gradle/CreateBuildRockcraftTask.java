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
package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.RockProjectSettings;
import com.canonical.rockcraft.util.BuildRunner;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CreateBuildRockcraftTask writes rockcraft.yaml for the build rock.
 */
public abstract class CreateBuildRockcraftTask extends DefaultTask {

    private final Logger logger = Logging.getLogger(CreateBuildRockcraftTask.class);

    private final BuildRockcraftOptions options;

    /**
     * Construct CreateBuildRockcraftTask
     * @param options - rockcraft project options
     */
    @Inject
    public CreateBuildRockcraftTask(BuildRockcraftOptions options) {
        super();
        this.options = options;
        if (options.getBuildGoals().length == 0) {
            options.setBuildGoals(new String[]{"build", "-x","checkRockcraft"});
        }
    }

    /**
     * Task action to write rockcraft.yaml for the build rock
     * @throws IOException - failed to write rockcraft.yaml
     * @throws InterruptedException - build was interrupted
     */
    @TaskAction
    @SuppressWarnings("unchecked")
    public void writeRockcraft() throws IOException, InterruptedException {
        RockProjectSettings settings = RockSettingsFactory.createBuildRockProjectSettings(getProject());

        ArrayList<File> artifacts = new ArrayList<>();
        Set<Object> dependsOn = getDependsOn();
        for (Object entry : dependsOn) {
            HashSet<Task> tasks = (HashSet<Task>) entry;
            for (Task task : tasks) {
                artifacts.addAll(task.getOutputs().getFiles().getFiles());
            }
        }
        if (options.isWithGradleCache()) {
            Path gradleBin = getProject().getGradle().getGradleHomeDir().toPath().resolve("bin/gradle");
            if (!Files.exists(gradleBin)) {
                throw new UnsupportedOperationException("Unable to export dependencies " +gradleBin + " does not exist!");
            }
            Path depUserHome = settings.getRockOutput().resolve(".gradle");
            createGradleCache(gradleBin, depUserHome);
            artifacts.add(depUserHome.toFile());
        }
        BuildRockCrafter crafter = new BuildRockCrafter(settings, options, artifacts);
        crafter.writeRockcraft();
    }

    private void createGradleCache(Path gradleBin, Path depUserHome) throws IOException, InterruptedException {
        ArrayList<String> args = new ArrayList<>(Arrays.asList(gradleBin.toString(),
                "--no-daemon",
                "--gradle-user-home",
                depUserHome.toString()));
        args.addAll(Arrays.asList(options.getBuildGoals()));
        int ret = BuildRunner.runBuild(logger::lifecycle, getProject().getRootDir(),
                args);
        if (ret != 0) {
            throw new UnsupportedOperationException("Failed to build task '"+ args.stream().collect(Collectors.joining(" ")) + "'" );
        }
    }
}
