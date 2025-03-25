package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockArchitecture;
import com.canonical.rockcraft.builder.RockProjectSettings;
import com.canonical.rockcraft.util.MavenArtifactCopy;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Writes build rock rockcraft file to the output directory
 */
@Mojo(name = "create-build-rock", threadSafe = false, requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public final class CreateBuildRockMojo extends AbstractMojo {

    @Component
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Component
    private RuntimeInformation runtimeInformation;

    @Parameter(property = "buildPackage")
    private final String buildPackage = "openjdk-21-jdk-headless";

    @Parameter(property = "targetRelease")
    private final int targetRelease = 21;

    @Parameter(property = "jlink")
    private final boolean jlink = false;

    @Parameter(property = "summary")
    private final String summary = "";

    @Parameter(property = "description")
    private final String description = null;

    @Parameter(property = "command")
    private final String command = "";

    @Parameter(property = "source")
    private String source;

    @Parameter(property = "branch")
    private String branch;

    @Parameter(property = "architectures")
    private final RockArchitecture[] architectures = new RockArchitecture[0];

    @Parameter(property = "slices")
    private final List<String> slices = new ArrayList<String>();

    @Parameter(property = "buildRockcraftYaml")
    private String buildRockcraftYaml;

    @Parameter(property = "service")
    private final boolean createService = true;

    private final BuildRockcraftOptions options = new BuildRockcraftOptions();

    /**
     * Creates CreateBuildRockMojo
     */
    public CreateBuildRockMojo() {}

    /**
     * Returns BuildRockcraftOptions initialized using plugin options
     *
     * @return initialized plugin options
     */
    private BuildRockcraftOptions getOptions() {
        return options;
    }

    /**
     * Returns current Maven project
     *
     * @return Maven Project object
     */
    private MavenProject getProject() {
        return project;
    }

    /**
     * Returns runtime information for Maven
     *
     * @return runtime information
     */
    private RuntimeInformation getRuntimeInformation() {
        return runtimeInformation;
    }

    private void configure() {
        options.setBuildPackage(buildPackage);
        options.setSummary(summary);
        options.setDescription(description);
        options.setSource(source);
        options.setBranch(branch);
        options.setArchitectures(architectures);
        options.setSlices(slices);
        options.setRockcraftYaml(buildRockcraftYaml);
    }

    public void execute() throws MojoExecutionException {
        configure();

        RockProjectSettings settings = RockSettingsFactory.createBuildRockProjectSettings(getRuntimeInformation(), getProject());
        Path dependenciesOutput = settings.getRockOutput().resolve(IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT);
        dependenciesOutput.toFile().mkdirs();
        executeMojo(
                plugin(groupId("org.apache.maven.plugins"),
                        artifactId("maven-dependency-plugin"),
                        version("3.8.1")),
                goal("go-offline"),
                configuration(),
                executionEnvironment(getProject(), session, pluginManager));
        try {
            MavenArtifactCopy artifactCopy = new MavenArtifactCopy(dependenciesOutput);
            String baseDir = session.getLocalRepository().getBasedir();
            Model model = getProject().getModel();
            for (Dependency dep : model.getDependencies()) {
                copyArtifacts(baseDir, dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), artifactCopy);
            }
            for (Plugin plugin : model.getBuild().getPlugins()) {
                copyArtifacts(baseDir, plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), artifactCopy);
            }
        }
        catch (IOException e) {
            throw new MojoExecutionException(e);
        }

        BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, getOptions(), Collections.singletonList(dependenciesOutput.toFile()));
        try {
            rockCrafter.writeRockcraft();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void copyArtifacts(String baseDir, String groupId, String artifactId, String versionId, MavenArtifactCopy artifactCopy) throws IOException {
        for (File f : getArtifactFiles(baseDir, groupId, artifactId, versionId)) {
            artifactCopy.copyToMavenRepository(f, groupId, artifactId, versionId);
        }
    }

    private File[] getArtifactFiles(String baseDir, String groupId, String artifactId, String versionId) {
        String artifactPath = baseDir + File.separator
                + groupId.replace('.', File.separatorChar) + File.separator
                + artifactId + File.separator
                + versionId;
        return new File(artifactPath).listFiles();
    }
}

