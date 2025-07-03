package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockArchitecture;
import com.canonical.rockcraft.builder.RockProjectSettings;
import com.canonical.rockcraft.util.MavenArtifactCopy;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.resolvers.GoOfflineMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.DependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Writes build rock rockcraft file to the output directory
 */
@Mojo(name = "create-build-rock", threadSafe = false, requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public final class CreateBuildRockMojo extends GoOfflineMojo {

    private HashSet<String> processed;

    @Component
    private ProjectBuilder projectBuilder;

    @Component
    private RuntimeInformation runtimeInformation;

    @Component
    private RepositorySystem repositorySystem;

    @Component
    private RemoteRepositoryManager remoteRepositoryManager;

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
    }

    /**
     * Executes the mojo. It refreshes dependencies in target/build-rock/dependencies and generates
     * target/build-rock/rockcraft.yaml.
     *
     * @throws MojoExecutionException - generation failure
     */
    protected void doExecute() throws MojoExecutionException {
        configure();

        RockProjectSettings settings = RockSettingsFactory.createBuildRockProjectSettings(getRuntimeInformation(), getProject());
        copyMaven(settings);

        Path dependenciesOutput = settings.getRockOutput().resolve(IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT);
        dependenciesOutput.toFile().mkdirs();
        try {
            processed = new HashSet<>();
            ProjectBuildingRequest buildingRequest = newResolveArtifactProjectBuildingRequest();
            MavenArtifactCopy artifactCopy = new MavenArtifactCopy(dependenciesOutput);
            String baseDir = session.getLocalRepository().getBasedir();
            for (Artifact dep : resolveDependencyArtifacts()) {
                copyArtifacts(baseDir, dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), artifactCopy);
                //resolveArtifactMetadata(buildingRequest, baseDir, artifactCopy, dep);
            }
            for (Artifact plugin : resolvePluginArtifacts()) {
                copyArtifacts(baseDir, plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), artifactCopy);
                //resolveArtifactMetadata(buildingRequest, baseDir, artifactCopy, plugin);
            }

            ProjectBuildingRequest projectBuildingRequest = session.getProjectBuildingRequest();

            // Get the remote repositories for the current project
            List<RemoteRepository> remoteRepositories = session.getCurrentProject().getRemotePluginRepositories();

            // Create the ModelResolver
            ModelResolver modelResolver = new ProjectModelResolver(
                    session.getRepositorySession(),
                    null, // RequestTrace, can be null
                    repositorySystem,
                    remoteRepositoryManager,
                    remoteRepositories,
                    ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
                    null // ReactorModelPool, can be null
            );

            DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
            DefaultModelBuilder builder = factory.newInstance();

            ModelBuildingRequest req = new DefaultModelBuildingRequest();
            req.setModelResolver(new DelegatingModelResolver(modelResolver, artifactCopy));
            req.setPomFile(getProject().getFile());
            req.setSystemProperties(System.getProperties());
            req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            ModelBuildingResult builtModel = builder.build(req);

            BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, getOptions(), Collections.singletonList(dependenciesOutput.toFile()));
            rockCrafter.writeRockcraft();
        }
        catch (IOException | DependencyResolverException | ModelBuildingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void copyMaven(RockProjectSettings settings) throws MojoExecutionException {
        final Path mavenOutput = settings.getRockOutput().resolve(IRockcraftNames.MAVEN_OUTPUT);
        try {
            try (Stream<Path> s = Files.walk(mavenHome.toPath())) {
              s.forEach(sourcePath -> {
                  try {
                      Path destinationPath = mavenOutput.resolve(mavenHome.toPath().relativize(sourcePath));
                      if (Files.isDirectory(sourcePath)) {
                          destinationPath.toFile().mkdirs();
                          return;
                      }
                      if (Files.isSymbolicLink(sourcePath)) {
                          Path resolvedSourcePath = Files.readSymbolicLink(sourcePath);
                          if (!resolvedSourcePath.isAbsolute()) {
                              resolvedSourcePath = sourcePath.getParent().resolve(resolvedSourcePath);
                          }
                          sourcePath = resolvedSourcePath;
                      }
                      if (Files.exists(sourcePath)) {
                        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                  } catch (IOException e) {
                      throw new RuntimeException("Failed to copy " + sourcePath, e);
                  }
              });
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }

    private void copyArtifacts(String baseDir, String groupId, String artifactId, String versionId, MavenArtifactCopy artifactCopy) throws IOException {
        for (File f : getArtifactFiles(baseDir, groupId, artifactId, versionId)) {
            if (artifactCopy.isProcessed(f)) {
                continue;
            }
            getLog().info("Copy "+ f);
            artifactCopy.copyToMavenRepository(f, groupId, artifactId, versionId);
        }
    }

    /**
     * Copies metadata poms (parent + dependency management)
     *
     * @param baseDir - destination
     * @param artifactCopy - artifactCopy utility
     */
    private void copyMetadataPOMs(MavenProject project, ProjectBuildingRequest buildingRequest, String baseDir, MavenArtifactCopy artifactCopy) throws DependencyResolverException, IOException {
        if (project == null) {
            return;
        }
        // copy parent pom metadata
        copyParent(project, buildingRequest, baseDir, artifactCopy);

        // copy boms, do not dive into other dependencies
        DependencyManagement dependencyManagement = project.getOriginalModel().getDependencyManagement();
        if (dependencyManagement == null) {
            return;
        }

        for (Dependency dep : dependencyManagement.getDependencies()) {
            resolveArtifact(buildingRequest, baseDir, artifactCopy, create(project, dep));
        }
    }

    private void copyParent(MavenProject project, ProjectBuildingRequest buildingRequest, String baseDir, MavenArtifactCopy artifactCopy) throws DependencyResolverException, IOException {
        Artifact art = project.getParentArtifact();
        if (art == null) {
            return;
        }

        String id = art.getArtifactId();
        if (id == null || id.isEmpty()) {
            return;
        }
        DefaultDependableCoordinate dep = create(art);
        resolveArtifact(buildingRequest, baseDir, artifactCopy, dep);
        copyMetadataPOMs(project.getParent(), buildingRequest, baseDir, artifactCopy);
    }

    private static DefaultDependableCoordinate create(Artifact art) {
        DefaultDependableCoordinate dep = new DefaultDependableCoordinate();
        dep.setType(art.getType());
        dep.setGroupId(art.getGroupId());
        dep.setArtifactId(art.getArtifactId());
        dep.setVersion(art.getVersion());
        dep.setClassifier(art.getClassifier());
        return dep;
    }

    private void resolveArtifact(ProjectBuildingRequest buildingRequest, String baseDir, MavenArtifactCopy artifactCopy, DependableCoordinate coordinate) throws IOException {
        try {
            Iterable<ArtifactResult> results = getDependencyResolver().resolveDependencies(buildingRequest, coordinate, null);
            for (ArtifactResult r : results) {

                resolveArtifactMetadata(buildingRequest, baseDir, artifactCopy, r.getArtifact());

                copyArtifacts(baseDir,
                        r.getArtifact().getGroupId(),
                        r.getArtifact().getArtifactId(),
                        r.getArtifact().getVersion(),
                        artifactCopy);
            }
        }
        catch (DependencyResolverException e) {
            getLog().warn("Failed to resolve "+ coordinate);
        }
    }

    private void resolveArtifactMetadata(ProjectBuildingRequest buildingRequest, String baseDir, MavenArtifactCopy artifactCopy, Artifact r) throws IOException, DependencyResolverException {
        if (processed.contains(r.toString())) {
            return;
        }
        processed.add(r.toString());
        try {
            File pomFile = r.getFile();
            if (pomFile.getName().endsWith(".jar")) {
                // replace any sequence starting with . and not containing .
                pomFile = new File(pomFile.getAbsolutePath().replaceAll("\\.[^.]+$", ".pom"));
            }
            if (pomFile.getName().endsWith(".pom") && pomFile.exists()) {
                ProjectBuildingRequest artifactBuildRequest = newResolveArtifactProjectBuildingRequest();
                artifactBuildRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
                MavenProject project = projectBuilder.build(pomFile, artifactBuildRequest).getProject();
                copyMetadataPOMs(project, buildingRequest, baseDir, artifactCopy);
            }
        } catch (ProjectBuildingException e) {
            throw new RuntimeException(e);
        }
    }

    private DependableCoordinate create(MavenProject project, Dependency dependency) {
        final DefaultDependableCoordinate result = new DefaultDependableCoordinate();
        result.setGroupId(getActualValue(project, dependency.getGroupId()));
        result.setArtifactId(getActualValue(project, dependency.getArtifactId()));
        result.setVersion(getActualValue(project, dependency.getVersion()));
        result.setType(dependency.getType());
        result.setClassifier(dependency.getClassifier());
        return result;
    }

    private String getActualValue(MavenProject project, String version) {
        // property format ${foo}
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(version);
        if (!matcher.matches()) {
            return version;
        }
        String value = matcher.group(1);
        HashMap<String, String> projectProperties = new HashMap<>();
        projectProperties.put("project.version", project.getVersion());
        projectProperties.put("project.groupId", project.getGroupId());
        projectProperties.put("project.artifactId", project.getArtifactId());
        String projectValue= projectProperties.get(value);
        if (projectValue != null) {
            return projectValue;
        }
        return String.valueOf(project.getProperties().get(value));
    }

    private File[] getArtifactFiles(String baseDir, String groupId, String artifactId, String versionId) {
        String artifactPath = baseDir + File.separator
                + groupId.replace('.', File.separatorChar) + File.separator
                + artifactId + File.separator
                + versionId;
        return new File(artifactPath).listFiles();
    }
}
