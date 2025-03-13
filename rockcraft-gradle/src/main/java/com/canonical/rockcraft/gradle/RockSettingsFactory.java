package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.IRockcraftNames;
import org.gradle.api.Project;

import com.canonical.rockcraft.builder.Generator;
import com.canonical.rockcraft.builder.RockProjectSettings;

/**
 * Creates RockProjectSettings from Gradle project
 */
public class RockSettingsFactory {

    /**
     * Constructs RockSettingsFactory
     */
    RockSettingsFactory() {}

    /**
     * Creates RockProjectSettings from Gradle project
     *
     * @param project - gradle project
     * @return RockProjectSettings
     */
    public static final RockProjectSettings createRockProjectSettings(Project project) {

        return new RockProjectSettings(Generator.gradle, project.getGradle().getGradleVersion(),
                project.getName(), String.valueOf(project.getVersion()),
                project.getProjectDir().toPath(), project.getLayout().getBuildDirectory().getAsFile().get().toPath(),
                !project.getTasksByName(ITaskNames.JLINK, false).isEmpty() ||
                        !project.getTasksByName(ITaskNames.RUNTIME, false).isEmpty());
    }

    /**
     * Creates RockProjectSettings from Gradle project for the build rock
     *
     * @param project - gradle project
     * @return RockProjectSettings
     */
    public static final RockProjectSettings createBuildRockProjectSettings(Project project) {
        return new RockProjectSettings(Generator.gradle, project.getGradle().getGradleVersion(),
                project.getName(), String.valueOf(project.getVersion()),
                project.getProjectDir().toPath(), project.getLayout().getBuildDirectory().getAsFile().get().toPath().resolve(IRockcraftNames.BUILD_ROCK_OUTPUT),
                !project.getTasksByName(ITaskNames.JLINK, false).isEmpty() ||
                        !project.getTasksByName(ITaskNames.RUNTIME, false).isEmpty());
    }

}
