package com.canonical.rockcraft.builder;

import java.nio.file.Path;

/**
 * Stores project settings of the generator (e.g. maven or gradle)
 */
public class RockProjectSettings {
    private final String name;
    private final String version;
    private final Path projectPath;
    private final String generatorName;
    private final Path rockOutput;

    /**
     * Constructs the rock project settings
     *
     * @param generatorName name of the generator
     * @param name          rockcraft project name
     * @param version       rockcraft project version
     * @param projectPath   path to the rockcraft project
     * @param rockOutput    path to where to generate rockcraft.yaml
     */
    public RockProjectSettings(String generatorName, String name, String version, Path projectPath, Path rockOutput) {
        this.generatorName = generatorName;
        this.name = name;
        this.version = version;
        this.projectPath = projectPath;
        this.rockOutput = rockOutput;
    }

    /**
     * Get the generator name
     */
    public String getGeneratorName() {
        return generatorName;
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

}