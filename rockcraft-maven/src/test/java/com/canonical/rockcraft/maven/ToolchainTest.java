package com.canonical.rockcraft.maven;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.toolchain.ToolchainManager;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;

import java.io.File;

public class ToolchainTest {

    @Test
    public void testJava8() {

        final Log mockLog = Mockito.mock(Log.class);

        final ToolchainManager mockManager = Mockito.mock(ToolchainManager.class);
        final org.apache.maven.toolchain.Toolchain mockToolchain
            = Mockito.mock(org.apache.maven.toolchain.Toolchain.class);

        StringBuilder output = new StringBuilder();
        Mockito.when(mockManager.getToolchainFromBuildContext(Mockito.any(), Mockito.any()))
            .thenReturn(mockToolchain);
        Mockito.when(mockToolchain.findTool("javac"))
            .thenReturn("/usr/lib/jvm/java-8-openjdk-amd64/bin/javac");
        doAnswer(invocation -> {
                output.append((String) invocation.getArgument(0));
                return null;
            }).when(mockLog).warn(Mockito.anyString());

        String result = Toolchain.getToolchainPackage(null, mockManager, mockLog);
        assertEquals("", output.toString());
        assertEquals("openjdk-8-jdk-headless", result);
    }

    @Test
    void testMissingJava() {
        final Log mockLog = Mockito.mock(Log.class);

        final ToolchainManager mockManager = Mockito.mock(ToolchainManager.class);
        final org.apache.maven.toolchain.Toolchain mockToolchain
            = Mockito.mock(org.apache.maven.toolchain.Toolchain.class);

        StringBuilder output = new StringBuilder();
        Mockito.when(mockManager.getToolchainFromBuildContext(Mockito.any(), Mockito.any()))
            .thenReturn(mockToolchain);
        Mockito.when(mockToolchain.findTool("javac"))
            .thenReturn("/usr/lib/jvm/java-missing-openjdk-amd64/bin/javac");
        doAnswer(invocation -> {
                System.err.println("Called warn with: " + invocation.getArgument(0));
                output.append((String) invocation.getArgument(0));
                return null;
            }).when(mockLog).warn(Mockito.anyString());

        doAnswer(invocation -> {
                System.err.println("Called warn with: " + invocation.getArgument(0));
                output.append((String) invocation.getArgument(0));
                return null;
            }).when(mockLog).warn(Mockito.anyString(), Mockito.any());

        String result = Toolchain.getToolchainPackage(null, mockManager, mockLog);
        assertEquals("java-rockcraft-plugin: Maven Toolchain - javac error", output.toString());
        assertEquals("openjdk-21-jdk", result);
    }

    @Test
    void testJava11() {
        // skip test if JDK11 is not installed
        if (!new File("/usr/lib/jvm/java-11-openjdk-amd64/bin/javac").exists()) {
            return;
        }

        final Log mockLog = Mockito.mock(Log.class);

        final ToolchainManager mockManager = Mockito.mock(ToolchainManager.class);
        final org.apache.maven.toolchain.Toolchain mockToolchain
            = Mockito.mock(org.apache.maven.toolchain.Toolchain.class);

        StringBuilder output = new StringBuilder();
        Mockito.when(mockManager.getToolchainFromBuildContext(Mockito.any(), Mockito.any()))
            .thenReturn(mockToolchain);
        Mockito.when(mockToolchain.findTool("javac"))
            .thenReturn("/usr/lib/jvm/java-11-openjdk-amd64/bin/javac");
        doAnswer(invocation -> {
                output.append((String) invocation.getArgument(0));
                return null;
            }).when(mockLog).warn(Mockito.anyString());

        String result = Toolchain.getToolchainPackage(null, mockManager, mockLog);
        assertEquals("", output.toString());
        assertEquals("openjdk-11-jdk", result);
    }

}
