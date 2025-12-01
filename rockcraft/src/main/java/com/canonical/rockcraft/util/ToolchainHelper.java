package com.canonical.rockcraft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

public final class ToolchainHelper {

    public static final String DEFAULT_JDK = "openjdk-21-jdk";
    private static final HashSet<String> SUPPORTED = new HashSet<>(Arrays.asList("11", "17", "21"));

    private ToolchainHelper() {}

    public enum Reason {
        REASON_OK,
        JAVAC_ERROR,
        JAVAC_VERSION_STRING,
        JAVAC_UNSUPPORTED_VERSION_STRING
    }

    public static class ToolchainPackage {
        private final String name;
        private final Reason reason;
        private final String rawOutput;

        public ToolchainPackage(String name, Reason reason, String output) {
            this.name = name;
            this.reason = reason;
            this.rawOutput = output;
        }
        public String getName() { return name; }
        public Reason getReason() { return reason; }
        public String getRawOutput() { return rawOutput; }
    }

    public static ToolchainPackage getBuildPackage(String tool) throws InterruptedException, IOException {
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
            return new ToolchainPackage(DEFAULT_JDK, Reason.JAVAC_ERROR, output.toString());
        }
        String[] text = output.toString().split(" ");
        if (text.length < 2 || !"javac".equals(text[0])) {
            return new ToolchainPackage(DEFAULT_JDK, Reason.JAVAC_VERSION_STRING, output.toString());
        }
        String version = text[1];
        String[] versions = version.split("\\.");
        if (versions.length < 2) {
            return new ToolchainPackage(DEFAULT_JDK, Reason.JAVAC_VERSION_STRING, output.toString());
        }
        String major = versions[0];
        String minor = versions[1];
        if ("1".equals(major) && "8".equals(minor)) { // 1.8
            return new ToolchainPackage("openjdk-8-jdk", Reason.REASON_OK, output.toString());
        }
        if (SUPPORTED.contains(major)) {
            return new ToolchainPackage("openjdk-" + major + "-jdk", Reason.REASON_OK, output.toString());
        }
        return new ToolchainPackage(DEFAULT_JDK, Reason.JAVAC_UNSUPPORTED_VERSION_STRING, output.toString());
    }
}
