package com.canonical.rockcraft.gradle;

import org.gradle.api.Project;

/**
 * Utility class to get toolchain settings
 */
public final class Toolchain {

    private Toolchain(){}

    /**
     * Gets the toolchain settings for the project
     * @param project current project
     * @return OpenJDK package
     */
    public static String getToolchainVersion(Project project) {

        return "openjdk-21-jdk";
    }
}
