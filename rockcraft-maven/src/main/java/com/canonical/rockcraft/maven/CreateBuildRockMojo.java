package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockArchitecture;
import com.canonical.rockcraft.builder.RockProjectSettings;
import com.canonical.rockcraft.util.BuildRunner;
import com.canonical.rockcraft.util.POMUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.eclipse.aether.RepositorySystemSession;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Writes build rock rockcraft file to the output directory
 */
@Mojo(name = "create-build-rock", threadSafe = false, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.INSTALL)
public final class CreateBuildRockMojo extends AbstractMojo {

    @Component
    private RuntimeInformation runtimeInformation;

    @Component
    private MavenProject project;

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

    @Parameter(defaultValue = "${maven.home}", readonly = true, required = true)
    private File mavenHome;

    @Parameter(property = "run-build.workingDirectory", defaultValue = "${project.basedir}")
    private File workingDirectory;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(property = "allowLocal", defaultValue = "false")
    private boolean allowLocal;

    @Parameter(property = "buildGoals", defaultValue = "package")
    private String[] buildGoals;

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
        options.setBuildGoals(buildGoals);
        options.setNativeImage(isNativeImageRequested());
    }

    /**
     * Executes the mojo. It refreshes dependencies in target/build-rock/dependencies and generates
     * target/build-rock/rockcraft.yaml.
     *
     * @throws MojoExecutionException - generation failure
     */
    public void execute() throws MojoExecutionException {
        configure();

        RockProjectSettings settings = RockSettingsFactory.createBuildRockProjectSettings(getRuntimeInformation(), project);
        Path dependenciesOutput = settings.getRockOutput().resolve(IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT);
        Path mavenExecutable = mavenHome.toPath().resolve("bin/mvn");
        if (!Files.exists(mavenExecutable)) {
            throw new MojoExecutionException(mavenExecutable + ": maven executable file does not exist!");
        }
        try {
            Path buildPom = project.getModel().getPomFile().toPath();
            if (allowLocal) {
                Path temp = Files.createTempFile(workingDirectory.toPath(), "tempPom", ".xml");
                temp.toFile().deleteOnExit();
                Files.copy(project.getModel().getPomFile().toPath(), temp , StandardCopyOption.REPLACE_EXISTING);

                File localRepo = repoSession.getLocalRepository().getBasedir();
                POMUtil.addRepositoryToPom(temp.toFile(),
                        "local-maven-cache"+ System.currentTimeMillis(),
                        "local maven repository",
                        "file://"+localRepo);
                buildPom = temp;
            }
            dependenciesOutput.toFile().mkdirs();
            List<String> args = new ArrayList<>(Arrays.asList("mvn",
                    "-Dmaven.repo.local="+dependenciesOutput,
                    "-f", buildPom.toString()));
            args.addAll(Arrays.asList(buildGoals));
            int exitCode = BuildRunner.runBuild(x ->  getLog().info(x), workingDirectory, args);

            if (exitCode != 0){
                throw new MojoExecutionException("Failed to build project "+ project.getName() + ", dependencies are not available");
            }

            BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, getOptions(), Collections.singletonList(dependenciesOutput.toFile()));
            rockCrafter.writeRockcraft();
        }
        catch (IOException | InterruptedException | ParserConfigurationException | SAXException | TransformerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean isNativeImageRequested() {
        List<String> activeProfiles = session.getRequest().getActiveProfiles();
        boolean nativeProfileActivated = activeProfiles.stream().anyMatch(profile -> "native".equals(profile));

        boolean nativeCompileGoalRequested = false;

        for (String goal : session.getGoals()) {
            if (goal.equals("native:compile") ||
                goal.equals("org.graalvm.buildtools:native-maven-plugin:compile") ||
                goal.contains(":native:compile")) {
                nativeCompileGoalRequested = true;
            }
        }
        return nativeProfileActivated && nativeCompileGoalRequested;
    }
}

