package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.util.ToolchainHelper;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.toolchain.ToolchainManager;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Utility class to get toolchain settings
 */
public final class Toolchain {

    private Toolchain() {
    }

    private static String getToolchainJavacPath(MavenSession session, ToolchainManager toolchainManager) {
        if (toolchainManager == null) {
            return null;
        }
        org.apache.maven.toolchain.Toolchain jdkToolchain = toolchainManager.getToolchainFromBuildContext("jdk", session);
        if (jdkToolchain == null) {
            return null;
        }
        return jdkToolchain.findTool("javac");
    }

    private static String getJavacPath(MavenSession session, ToolchainManager toolchainManager, Log log) {
        String where = getToolchainJavacPath(session, toolchainManager);
        if (where != null) {
            return where;
        }
        String javaHome = System.getProperty("java.home");
        if (javaHome == null || javaHome.isEmpty()) {
            log.warn("java-rockcraft-plugin: please configure Maven toolchain or provide a valid Java Home.");
            return null;
        }
        Path java9Path = Paths.get(javaHome, "bin", "javac");
        Path java8Path = Paths.get(Paths.get(javaHome).getParent().toString(), "bin", "javac");

        if (Files.isExecutable(java9Path)) {
            return java9Path.toString();
        } else if (Files.isExecutable(java8Path)) {
            return java8Path.toString();
        }
        log.warn("java-rockcraft-plugin: please configure Maven toolchain or provide a valid Java Home: "+ javaHome);
        return null;
    }

    /**
     * Gets the toolchain settings for the project
     *
     * @param session          - maven session
     * @param toolchainManager - toolchain manager
     * @param log              - maven logger
     * @return OpenJDK package
     */
    public static String getToolchainPackage(MavenSession session, ToolchainManager toolchainManager, Log log) {
        try {
            String tool = getJavacPath(session, toolchainManager, log);
            if (tool == null) {
                return ToolchainHelper.DEFAULT_JDK;
            }

            ToolchainHelper.ToolchainPackage p = ToolchainHelper.getBuildPackage(tool);
            switch (p.getReason()) {
                case JAVAC_ERROR:
                    log.warn(String.format("java-rockcraft-plugin: Maven Toolchain - javac error %s, please set buildPackage configuration option: %s", tool, p.getRawOutput()));
                    break;
                case JAVAC_VERSION_STRING:
                    log.warn(String.format("java-rockcraft-plugin: Maven Toolchain - unable to parse javac version string, please set buildPackage configuration option: %s", p.getRawOutput()));
                    break;
                case JAVAC_UNSUPPORTED_VERSION_STRING:
                    log.warn(String.format("java-rockcraft-plugin: Maven Toolchain - unsupported version string, please set buildPackage configuration option. %s", p.getRawOutput()));
                    break;
                default:
                    break;
            }
            return p.getName();
        } catch (IOException | InterruptedException e) {
            log.warn("java-rockcraft-plugin: Maven Toolchain - javac error", e);
        }
        return ToolchainHelper.DEFAULT_JDK;
    }
}
