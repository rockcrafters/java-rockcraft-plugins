package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.util.ToolchainHelper;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.toolchain.ToolchainManager;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Utility class to get toolchain settings
 */
public final class Toolchain {

    private Toolchain() {
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
            if (toolchainManager == null) {
                log.warn("java-rockcraft-plugin: Maven Toolchain manager is not present.");
                return ToolchainHelper.DEFAULT_JDK;
            }
            System.err.println("---- has toolchain manager --- ");
            org.apache.maven.toolchain.Toolchain jdkToolchain = toolchainManager.getToolchainFromBuildContext("jdk", session);
            if (jdkToolchain == null) {
                log.warn("java-rockcraft-plugin: Maven Toolchain is not configured. Please configure toolchain or use buildPackage configuration");
                return ToolchainHelper.DEFAULT_JDK;
            }
            String tool = jdkToolchain.findTool("javac");
            if (tool == null) {
                log.warn("java-rockcraft-plugin: Maven Toolchain - javac tool is not found.");
                return ToolchainHelper.DEFAULT_JDK;
            }
            ToolchainHelper.ToolchainPackage p = ToolchainHelper.getBuildPackage(tool);
            switch (p.getReason()) {
                case JAVAC_ERROR:
                    log.warn("java-rockcraft-plugin: Maven Toolchain - javac error {}, please set buildPackage configuration option: {}", tool, p.getRawOutput());
                    break;
                case JAVAC_VERSION_STRING:
                    log.warn("java-rockcraft-plugin: Maven Toolchain - unable to parse javac version string, please set buildPackage configuration option: {}", p.getRawOutput());
                    break;
                case JAVAC_UNSUPPORTED_VERSION_STRING:
                    log.warn("java-rockcraft-plugin: Maven Toolchain - unsupported version string, please set buildPackage configuration option. {}", p.getRawOutput());
                    break;
            }
            return p.getName();
        } catch (IOException | InterruptedException e) {
            log.warn("java-rockcraft-plugin: Maven Toolchain - javac error", e);
        }
        return ToolchainHelper.DEFAULT_JDK;
    }
}
