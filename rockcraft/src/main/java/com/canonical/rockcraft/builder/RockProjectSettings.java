package com.canonical.rockcraft.builder;

import java.nio.file.Path;

/**
 * Stores project settings of the generator (e.g. maven or gradle)
 */
public class RockProjectSettings {
    private final String name;
    private final String version;
    private final Path projectPath;
    private final Generator generatorName;
    private final String generatorVersion;
    private final Path rockOutput;
    private final boolean beryxJlink;

    /**
     * Constructs the rock project settings
     *
     * @param generatorName name of the generator
     * @param generatorVersion version of the generator
     * @param name          rockcraft project name
     * @param version       rockcraft project version
     * @param projectPath   path to the rockcraft project
     * @param rockOutput    path to where to generate rockcraft.yaml
     * @param beryxJlink    whether to copy Beryx jlink image to the rock
     */
    public RockProjectSettings(Generator generatorName, String generatorVersion, String name, String version, Path projectPath, Path rockOutput, boolean beryxJlink) {
        this.generatorName = generatorName;
        this.generatorVersion = generatorVersion;
        this.name = name;
        this.version = version;
        this.projectPath = projectPath;
        this.rockOutput = rockOutput;
        this.beryxJlink = beryxJlink;
    }

    /**
     * Get the generator name
     *
     * @return generator name
     */
    public Generator getGeneratorName() {
        return generatorName;
    }

    /**
     * Get the generator version
     *
     * @return generator version
     */
    public String getGeneratorVersion() {
        return generatorVersion;
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
