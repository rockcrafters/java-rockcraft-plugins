package com.canonical.rockcraft.gradle;

import org.gradle.api.Project;

import com.canonical.rockcraft.builder.Generator;
import com.canonical.rockcraft.builder.RockProjectSettings;
import org.gradle.api.invocation.Gradle;

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

        return new RockProjectSettings(Generator.gradle, project.getName(),
            String.valueOf(project.getVersion()), project.getGradle().getGradleVersion(),
                project.getProjectDir().toPath(), project.getLayout().getBuildDirectory().getAsFile().get().toPath(),
                !project.getTasksByName(ITaskNames.JLINK, false).isEmpty() ||
                        !project.getTasksByName(ITaskNames.RUNTIME, false).isEmpty());
    }
}
