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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
@Mojo(name = "create-build-rock", threadSafe = false, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE)
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

    @Parameter(property = "buildCommand", defaultValue = "package")
    private String buildArgs;

    @Parameter(property = "run-build.workingDirectory", defaultValue = "${project.basedir}")
    private File workingDirectory;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter(property = "allowLocalCache", defaultValue = "true")
    private boolean allowLocalCache;

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
            if (allowLocalCache) {
                Path temp = Files.createTempFile(workingDirectory.toPath(), "tempPom", ".xml");
                temp.toFile().deleteOnExit();
                Files.copy(project.getModel().getPomFile().toPath(), temp , StandardCopyOption.REPLACE_EXISTING);

                File localRepo = repoSession.getLocalRepository().getBasedir();

                addRepositoryToPom(temp.toFile(),
                        "local-maven-cache"+ System.currentTimeMillis(),
                        "local maven repository",
                        "file://"+localRepo);
                buildPom = temp;
            }

            ProcessBuilder pb = new ProcessBuilder("mvn",
                    "-Dmaven.repo.local="+dependenciesOutput,
                    "-f", buildPom.toString(),
                    buildArgs)
                    .redirectErrorStream(true)
                    .directory(workingDirectory);
            dependenciesOutput.toFile().mkdirs();

            Process process = pb.start();

            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    getLog().info(new String(buffer, 0, len).trim());
                }
            }
            int exitCode = process.waitFor();
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

    public static void addRepositoryToPom(File pomFile,
                                          String repoId,
                                          String repoName,
                                          String repoUrl)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        // Get the project element
        Node projectNode = doc.getElementsByTagName("project").item(0);
        if (projectNode == null) {
            throw new IllegalArgumentException("Invalid POM file: 'project' element not found.");
        }

        // Find or create the <repositories> element
        Node repositoriesNode = null;
        Node pluginRepositoriesNode = null;
        NodeList projectChildren = projectNode.getChildNodes();
        for (int i = 0; i < projectChildren.getLength(); i++) {
            Node child = projectChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("repositories")) {
                repositoriesNode = child;
            }
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("pluginRepositories")) {
                pluginRepositoriesNode = child;
            }

        }

        if (pluginRepositoriesNode == null) {
            // <repositories> does not exist, create it and append to <project>
            pluginRepositoriesNode = doc.createElement("pluginRepositories");
            projectNode.appendChild(pluginRepositoriesNode);
        }
        if (repositoriesNode == null) {
            // <repositories> does not exist, create it and append to <project>
            repositoriesNode = doc.createElement("repositories");
            projectNode.appendChild(repositoriesNode);
        }

        // Create the new <repository> element
        Element repositoryElement = doc.createElement("repository");

        appendRepositoryDefinition(repoId, repoName, repoUrl, doc, repositoryElement);

        // Append the new <repository> to <repositories>
        repositoriesNode.appendChild(repositoryElement);

        Element pluginRepositoryElement = doc.createElement("pluginRepository");
        appendRepositoryDefinition(repoId, repoName, repoUrl, doc, pluginRepositoryElement);
        pluginRepositoriesNode.appendChild(pluginRepositoryElement);


        // Write the updated XML back to the file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // For pretty printing the XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Adjust indent spaces

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(pomFile);
        transformer.transform(source, result);

        System.out.println("Repository added/updated successfully in " + pomFile.getAbsolutePath());
    }

    private static void appendRepositoryDefinition(String repoId, String repoName, String repoUrl, Document doc, Element repositoryElement) {
        Element idElement = doc.createElement("id");
        idElement.setTextContent(repoId);
        repositoryElement.appendChild(idElement);

        Element nameElement = doc.createElement("name");
        nameElement.setTextContent(repoName);
        repositoryElement.appendChild(nameElement);

        Element urlElement = doc.createElement("url");
        urlElement.setTextContent(repoUrl);
        repositoryElement.appendChild(urlElement);
    }
}

