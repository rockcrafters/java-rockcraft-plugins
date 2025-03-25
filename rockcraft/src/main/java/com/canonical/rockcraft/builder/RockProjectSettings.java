package com.canonical.rockcraft.builder;

import java.nio.file.Path;

/**
 * Stores project settings of the generator (e.g. maven or gradle)
 */
public class RockProjectSettings {
    private final String name;
    private final String version;
    private final Path projectPath;
    private final BuildSystem buildSystem;
    private final String buildSystemVersion;
    private final Path rockOutput;
    private final boolean beryxJlink;

    /**
     * Constructs the rock project settings
     *
     * @param buildSystem name of the build system
     * @param buildSystemVersion version of the build system
     * @param name          rockcraft project name
     * @param version       rockcraft project version
     * @param projectPath   path to the rockcraft project
     * @param rockOutput    path to where to generate rockcraft.yaml
     * @param beryxJlink    whether to copy Beryx jlink image to the rock
     */
    public RockProjectSettings(BuildSystem buildSystem, String buildSystemVersion, String name, String version, Path projectPath, Path rockOutput, boolean beryxJlink) {
        this.buildSystem = buildSystem;
        this.buildSystemVersion = buildSystemVersion;
        this.name = name;
        this.version = version;
        this.projectPath = projectPath;
        this.rockOutput = rockOutput;
        this.beryxJlink = beryxJlink;
    }

    /**
     * Get the build system
     *
     * @return build system
     */
    public BuildSystem getBuildSystem() {
        return buildSystem;
    }

    /**
     * Get the build system version
     *
     * @return build system version
     */
    public String getBuildSystemVersion() {
        return buildSystemVersion;
    }

    /**
     * Get the project name
     *
     * @return project name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the project version
     *
     * @return the project version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the project root path
     *
     * @return project root path
     */
    public Path getProjectPath() {
        return projectPath;
    }

    /**
     * Get the rockcraft.yaml directory
     *
     * @return rockcraft.yaml directory
     */
    public Path getRockOutput() {
        return rockOutput;
    }

    /**
     * Get Beryx jlink plugin
     *
     * @return use Beryx Jlink plugin
     */
    public boolean getBeryxJLink() { return beryxJlink; }
}
