package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.util.ToolchainHelper;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.jvm.toolchain.JavaToolchainService;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to get toolchain settings
 */
public final class Toolchain {

    private Toolchain() {
    }

    /**
     * Gets the toolchain settings for the project
     *
     * @param project current project
     * @param service - Java toolchain service
     * @return OpenJDK package
     */
    public static String getToolchainPackage(Project project, JavaToolchainService service, Logger log) {
        JavaPluginExtension ext = project.getExtensions().getByType(JavaPluginExtension.class);
        File compiler = service.compilerFor(ext.getToolchain()).get().getExecutablePath().getAsFile();
        String tool = compiler.getAbsolutePath();
        try {
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
