package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockArchitecture;
import com.canonical.rockcraft.builder.RockProjectSettings;
import com.canonical.rockcraft.util.MavenArtifactCopy;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.lifecycle.internal.LifecycleDependencyResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.resolvers.GoOfflineMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.impl.RemoteRepositoryManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Writes build rock rockcraft file to the output directory
 */
@Mojo(name = "create-build-rock", threadSafe = false, requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public final class CreateBuildRockMojo extends GoOfflineMojo {

    private final Collection<String> collectionScopes = Arrays.asList("system", "compile", "test", "provided", "runtime");
    private final Collection<String> resolveScopes = Arrays.asList("system", "compile", "test", "provided", "runtime");

    @Component
    private LifecycleDependencyResolver lifecycleDependencyResolver;

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

        Path dependenciesOutput = settings.getRockOutput().resolve(IRockcraftNames.DEPENDENCIES_ROCK_OUTPUT);
        dependenciesOutput.toFile().mkdirs();
        try {
            // resolve the project
            MavenArtifactCopy artifactCopy = new MavenArtifactCopy(dependenciesOutput);
            String baseDir = session.getLocalRepository().getBasedir();

            Set<Artifact> myArtifacts = new HashSet<>();
            MavenProject copy = new MavenProject(getProject());
            copy.setArtifactFilter(new ArtifactFilter() {
                @Override
                public boolean include(Artifact artifact) {
                    return true;
                }
            });
            // resolve lifecycle artifacts
            lifecycleDependencyResolver.resolveProjectDependencies(copy,
                    collectionScopes,
                    resolveScopes,
                    session,
                    false,
                    myArtifacts);
            myArtifacts.addAll(copy.getArtifacts());
            myArtifacts.addAll(resolveDependencyArtifacts());
            myArtifacts.addAll(resolvePlugins());

            for (Artifact dep : myArtifacts) {
                copyArtifacts(baseDir, dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), artifactCopy);
                File pomFile = POMLookup.lookup(dep,
                        session.getCurrentProject().getRemotePluginRepositories(),
                        session.getRepositorySession(),
                        repositorySystem);
                // copies parent poms
                copyPOMFiles(artifactCopy, pomFile);
            }

            // copies poms for parent/dependencies
            copyPOMFiles(artifactCopy, getProject().getFile());

            BuildRockCrafter rockCrafter = new BuildRockCrafter(settings, getOptions(), Collections.singletonList(dependenciesOutput.toFile()));
            rockCrafter.writeRockcraft();
        }
        catch (IOException | DependencyResolverException | ModelBuildingException | LifecycleExecutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Set<Artifact> resolvePlugins() throws DependencyResolverException {
        HashSet<Artifact> arts = new HashSet<>();

        for (org.apache.maven.model.Plugin p : getProject().getBuildPlugins()) {
            if (!p.getDependencies().isEmpty()) {
                System.out.println("Foo");
            }
            DefaultDependableCoordinate dc = new DefaultDependableCoordinate();
            dc.setGroupId(p.getGroupId());
            dc.setArtifactId(p.getArtifactId());
            dc.setVersion(p.getVersion());
            Iterable<ArtifactResult> result = getDependencyResolver()
                    .resolveDependencies(newResolveArtifactProjectBuildingRequest(), dc, null);
            for (ArtifactResult r :  result) {
                arts.add(r.getArtifact());
            }
        }
        for (org.apache.maven.model.ReportPlugin p : getProject().getReportPlugins()) {
            DefaultDependableCoordinate dc = new DefaultDependableCoordinate();
            dc.setGroupId(p.getGroupId());
            dc.setArtifactId(p.getArtifactId());
            dc.setVersion(p.getVersion());
            Iterable<ArtifactResult> result = getDependencyResolver()
                    .resolveDependencies(newResolveArtifactProjectBuildingRequest(), dc, null);
            for (ArtifactResult r :  result) {
                arts.add(r.getArtifact());
            }
        }
        DefaultDependableCoordinate dc = new DefaultDependableCoordinate();
        dc.setGroupId("org.codehaus.plexus");
        dc.setArtifactId("plexus-utils");
        dc.setVersion("1.1");
        Iterable<ArtifactResult> result = getDependencyResolver()
                .resolveDependencies(newResolveArtifactProjectBuildingRequest(), dc, null);
        for (ArtifactResult r :  result) {
            arts.add(r.getArtifact());
        }

        return arts;
    }

    private void copyPOMFiles(MavenArtifactCopy artifactCopy, File pomFile) throws ModelBuildingException {
        ProjectBuildingRequest buildingRequest = newResolveArtifactProjectBuildingRequest();
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();
        DelegatingModelResolver resolver = new DelegatingModelResolver(new ProjectModelResolver(
                session.getRepositorySession(),
                null, // RequestTrace, can be null
                repositorySystem,
                remoteRepositoryManager,
                session.getCurrentProject().getRemotePluginRepositories(),
                ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
                null // ReactorModelPool, can be null
        ), artifactCopy);

        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setModelResolver(resolver);
        req.setPomFile(pomFile);
        req.setSystemProperties(System.getProperties());
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        req.setActiveProfileIds(buildingRequest.getActiveProfileIds());
        req.setInactiveProfileIds(buildingRequest.getInactiveProfileIds());
        req.setProfiles(buildingRequest.getProfiles());
        ModelBuildingResult modelResult = builder.build(req);
        modelResult.getEffectiveModel();
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

