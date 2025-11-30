package com.canonical.rockcraft.maven;

import org.apache.maven.project.MavenProject;

/**
 * Utility class to get toolchain settings
 */
public final class Toolchain {

    private Toolchain(){}

    /**
     * Gets the toolchain settings for the project
     * @param project
     * @return OpenJDK package
     */
    public static String getToolchainVersion(MavenProject project) {

        return "openjdk-21-jdk";
    }
}
