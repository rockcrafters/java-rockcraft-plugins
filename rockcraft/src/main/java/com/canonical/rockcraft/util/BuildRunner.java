package com.canonical.rockcraft.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs build using Gradle or Maven
 */
public class BuildRunner {

    public static int runBuild(Consumer<String> log, File directory, List<String> commandLine) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commandLine)
                .redirectErrorStream(true)
                .directory(directory);

        Process process = pb.start();

        try (InputStream is = process.getInputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                log.accept(new String(buffer, 0, len).trim());
            }
        }
        return process.waitFor();
    }
}
