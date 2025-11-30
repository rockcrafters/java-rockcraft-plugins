package com.canonical.rockcraft.maven;

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

    private static String DEFAULT_JDK = "openjdk-21-jdk";
    private static HashSet<String> SUPPORTED = new HashSet<>(Arrays.asList("11", "17", "21", "25"));

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
                System.err.println("---- toolchain is null--- ");
                return DEFAULT_JDK;
            }
            System.err.println("---- has toolchain manager --- ");
            org.apache.maven.toolchain.Toolchain jdkToolchain = toolchainManager.getToolchainFromBuildContext("jdk", session);
            if (jdkToolchain == null) {
                log.warn("java-rockcraft-plugin: Maven Toolchain is not configured. Please configure toolchain or use buildPackage configuration");
                System.err.println("---- NO TOOLCHAIN --- ");
                return DEFAULT_JDK;
            }
            String tool = jdkToolchain.findTool("javac");
            if (tool == null) {
                log.warn("java-rockcraft-plugin: Maven Toolchain - javac tool is not found.");
                System.err.println("---- NO TOOL --- ");
                return DEFAULT_JDK;
            }
            Process p = new ProcessBuilder(tool, "-version")
                    .redirectErrorStream(true)
                    .start();
            int code = p.waitFor();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                    output.append("\n");
                }
            }

            if (code != 0) {
                log.warn("java-rockcraft-plugin: Maven Toolchain - javac error " + code + ": " + output.toString());
                return DEFAULT_JDK;
            }
            String[] text = output.toString().split(" ");
            if (text.length < 2 || !"javac".equals(text[0])) {
                log.warn("java-rockcraft-plugin: Maven Toolchain - unable to parse javac version string: " + output.toString());
                return DEFAULT_JDK;
            }
            String version = text[1];
            String[] versions = version.split("\\.");
            if (versions.length < 2) {
                log.warn("java-rockcraft-plugin: Maven Toolchain - unable to parse javac version string: " + output.toString());
                return DEFAULT_JDK;
            }
            String major = versions[0];
            String minor = versions[1];
            if ("1".equals(major) && "8".equals(minor)) { // 1.8
                return "openjdk-8-jdk";
            }
            if (SUPPORTED.contains(major)) {
                return "openjdk-" + major + "-jdk";
            }
            log.warn("java-rockcraft-plugin: Maven Toolchain - unsupported version string, please set buildPackage configuration option. " + output.toString());
        }
        catch (IOException | InterruptedException e) {
            log.warn("java-rockcraft-plugin: Maven Toolchain - javac error", e);
        }
        return DEFAULT_JDK;
    }
}
