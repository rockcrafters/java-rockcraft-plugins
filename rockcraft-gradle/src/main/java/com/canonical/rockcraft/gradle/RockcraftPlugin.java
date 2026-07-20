/*
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

import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.DependencyOptions;
import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockBuilder;
import com.canonical.rockcraft.builder.RockcraftOptions;
import com.canonical.rockcraft.gradle.dependencies.DependencyExportTask;
import com.google.gradle.osdetector.OsDetector;
import com.google.gradle.osdetector.OsDetectorPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.util.Set;

/**
 * Gradle plugin for Rockcraft.
 * Allows to build rock images for Gradle projects.
 */
public class RockcraftPlugin implements Plugin<Project> {

    private final Logger logger = Logging.getLogger(RockcraftPlugin.class);

    /**
     * Constructs RockcraftPlugin
     */
    public RockcraftPlugin() {
        super();
    }


    /** Settings property name that disables all rockcraft tasks */
    static final String ROCKCRAFT_DISABLED_PROPERTY = "rockcraft-disabled";

    /**
     * Applies the plugin
     *
     * @param project The target object
     */
    public void apply(Project project) {

        if (project.hasProperty(ROCKCRAFT_DISABLED_PROPERTY)) {
            logger.log(LogLevel.WARN, "Rockcraft plugin tasks are disabled by the '{}' property", ROCKCRAFT_DISABLED_PROPERTY);
            registerNoOpTasks(project);
            return;
        }

        project.getPlugins().apply(OsDetectorPlugin.class);

        RockcraftOptions options = project.getExtensions().create("rockcraft", RockcraftOptions.class);

        OsDetector detector = project.getExtensions().getByType(OsDetector.class);

        if (!"linux".equals(detector.getOs()))
            throw new UnsupportedOperationException("Rockcraft is only supported on linux systems");

        DependencyOptions dependencyOptions = project.getExtensions().create("dependenciesExport", DependencyOptions.class);
        TaskProvider<DependencyExportTask> exportTask = project.getTasks()
                .register(ITaskNames.DEPENDENCIES, DependencyExportTask.class, dependencyOptions);
        exportTask.configure( dependencyExportTask -> {
            File buildDirectory = dependencyExportTask
                    .getProject()
                    .getLayout()
                    .getBuildDirectory()
                    .getAsFile().get();
            Path output = buildDirectory.toPath().resolve(String.format("%s%s%s", IRockcraftNames.BUILD_ROCK_OUTPUT, File.separator, IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT));
            dependencyExportTask.getOutputDirectory()
                    .set(output.toFile());
        });

        BuildRockcraftOptions buildOptions = project.getExtensions().create("buildRockcraft", BuildRockcraftOptions .class);
        buildOptions.setNativeImage(isNativeCompile(project));

        project.getTasks()
                .register(ITaskNames.CREATE_BUILD_ROCK, CreateBuildRockcraftTask.class, buildOptions);
        project.getTasks()
                .getByName(ITaskNames.CREATE_BUILD_ROCK)
                .dependsOn(project.getTasksByName(ITaskNames.DEPENDENCIES, false));
        project.getTasks()
                .register(ITaskNames.BUILD_BUILD_ROCK, BuildBuildRockcraftTask.class, buildOptions);
        project.getTasks()
                .getByName(ITaskNames.BUILD_BUILD_ROCK)
                .dependsOn(project.getTasksByName(ITaskNames.CREATE_BUILD_ROCK, false));
        project.getTasks()
                .register(ITaskNames.PUSH_BUILD_ROCK, PushBuildRockcraftTask.class, buildOptions);
        project.getTasks()
                .getByName(ITaskNames.PUSH_BUILD_ROCK)
                .dependsOn(project.getTasksByName(ITaskNames.BUILD_BUILD_ROCK, false));


        TaskProvider<Task> checkTask = project.getTasks().register(ITaskNames.CHECK_ROCKCRAFT, s -> {
            s.doFirst(x -> {
                try {
                    RockBuilder.checkRockcraft();
                } catch (IOException | InterruptedException e) {
                    throw new UnsupportedOperationException(e.getMessage());
                }
            });
        });

        project.getTasks()
                .getByName(ITaskNames.BUILD_BUILD_ROCK)
                .dependsOn(checkTask);

        Set<Task> tasks;
        String deploymentTask = options.getDistTask();
        if (deploymentTask == null) {
            tasks = project.getTasksByName(ITaskNames.JLINK, false);
            if (tasks.isEmpty())
                tasks = project.getTasksByName(ITaskNames.RUNTIME, false);
            if (tasks.isEmpty())
                tasks = project.getTasksByName(ITaskNames.BOOT_JAR, false);
            if (tasks.isEmpty())
                tasks = project.getTasksByName(ITaskNames.JAR, false);
        } else {
            tasks = project.getTasksByName(deploymentTask, false);
        }

        if (tasks.isEmpty()) {
            logger.log(LogLevel.WARN, "create-rock requires jlink, runtime, bootJar, jar task or a valid task name in rockcraft { distTask ='taskName' }, task is not available");
            return;
        }

        project.getTasks().register(ITaskNames.PUSH_ROCK, PushRockcraftTask.class, options);

        options.setNativeImage(isNativeCompile(project));

        TaskProvider<BuildRockcraftTask> build = project.getTasks().register(ITaskNames.BUILD_ROCK, BuildRockcraftTask.class, options);
        TaskProvider<CreateRockcraftTask> create = project.getTasks().register(ITaskNames.CREATE_ROCK, CreateRockcraftTask.class, options);

        project.getTasks().getByName(ITaskNames.PUSH_ROCK)
                .dependsOn(build);

        project.getTasks().getByName(ITaskNames.BUILD_ROCK)
                .dependsOn(create)
                        .dependsOn(checkTask);

        project.getTasks().getByName(ITaskNames.BUILD_ROCK)
                .dependsOn(create);

        project.getTasks().getByName(ITaskNames.CREATE_ROCK)
                .dependsOn(tasks);
    }

    private void registerNoOpTasks(Project project) {
        for (String taskName : new String[]{
                ITaskNames.DEPENDENCIES,
                ITaskNames.CREATE_BUILD_ROCK,
                ITaskNames.BUILD_BUILD_ROCK,
                ITaskNames.PUSH_BUILD_ROCK,
                ITaskNames.CHECK_ROCKCRAFT,
                ITaskNames.CREATE_ROCK,
                ITaskNames.BUILD_ROCK,
                ITaskNames.PUSH_ROCK
        }) {
            project.getTasks().register(taskName, t -> t.setGroup("rockcraft"));
        }
    }

    private boolean isNativeCompile(Project project) {
        return project.getGradle().getStartParameter().getTaskNames().contains("nativeCompile");
    }
}
