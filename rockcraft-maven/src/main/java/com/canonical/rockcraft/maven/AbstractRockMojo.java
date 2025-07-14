/**
 * Copyright 2024 Canonical Ltd.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.RockArchitecture;
import com.canonical.rockcraft.builder.RockcraftOptions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.execution.MavenSession;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for all rock mojos that initializes
 * RockcraftOptions
 */
public abstract class AbstractRockMojo extends AbstractMojo {

    /**
     * No specific initialization
     */
    public AbstractRockMojo() {}

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private RuntimeInformation runtimeInformation;

    @Parameter(property = "buildPackage")
    private String buildPackage = "openjdk-21-jdk";

    @Parameter(property = "targetRelease")
    private int targetRelease = 21;

    @Parameter(property = "jlink")
    private boolean jlink = false;

    @Parameter(property = "summary")
    private String summary = "";

    @Parameter(property = "description")
    private String description = null;

    @Parameter(property = "command")
    private String command = "";

    @Parameter(property = "source")
    private String source;

    @Parameter(property = "branch")
    private String branch;

    @Parameter(property = "architectures")
    private RockArchitecture[] architectures = new RockArchitecture[0];

    @Parameter(property = "slices")
    private List<String> slices = new ArrayList<String>();

    @Parameter(property = "rockcraftYaml")
    private String rockcraftYaml;

    @Parameter(property = "service")
    private boolean createService = true;

    private RockcraftOptions options = new RockcraftOptions();

    /**
     * Returns RockCraftOptions initialized using plugin options
     *
     * @return initialized plugin options
     */
    protected RockcraftOptions getOptions() {
        return options;
    }

    /**
     * Returns current Maven project
     *
     * @return Maven Project object
     */
    protected MavenProject getProject() {
        return project;
    }

    protected MavenSession getSession() {
        return session;
    }

    /**
     * Returns runtime information for Maven
     *
     * @return runtime information
     */
    protected RuntimeInformation getRuntimeInformation() { return runtimeInformation; }

    /**
     * Executes mojo. Initializes RockcraftOptions using plugin
     * configuration
     */
    @Override
    public void execute() throws MojoExecutionException {
        options.setBuildPackage(buildPackage);
        options.setTargetRelease(targetRelease);
        options.setJlink(jlink);
        options.setSummary(summary);
        options.setDescription(description);
        options.setCommand(command);
        options.setSource(source);
        options.setBranch(branch);
        options.setArchitectures(architectures);
        options.setSlices(slices);
        options.setRockcraftYaml(rockcraftYaml);
        options.setCreateService(createService);
        options.setNativeImage(isNativeImageRequested());
    }

    private boolean isNativeImageRequested() {
        List<String> activeProfiles = session.getRequest().getActiveProfiles();
        boolean nativeProfileActivated = activeProfiles.stream().anyMatch(profile -> "native".equals(profile));

        boolean nativeCompileGoalRequested = false;

        for (String goal : session.getGoals()) {
            if (goal.equals("native:compile") ||
                goal.equals("org.graalvm.buildtools:native-maven-plugin:compile")) {
                nativeCompileGoalRequested = true;
            }
        }
        return nativeProfileActivated && nativeCompileGoalRequested;
    }
}
