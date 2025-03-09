package com.canonical.rockcraft.gradle;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;
import org.gradle.maven.MavenModule;
import org.gradle.maven.MavenPomArtifact;
import org.jetbrains.annotations.NotNull;

public class DependencyExportTask extends DefaultTask {
    private ArrayList<Artifact> workQueue = new ArrayList();

    public DependencyExportTask() {
    }

    @TaskAction
    public void export() {
        PomResolver resolver = new PomResolver(this.getProject());
        Configuration implementation = (Configuration)this.getProject().getConfigurations().findByName("runtimeClasspath");
        this.copyPoms(this.getProject(), implementation, resolver);
    }

    protected void copyPoms(Project project, Configuration config, ModelResolver modelResolver) {
        DefaultModelBuilderFactory factory = new DefaultModelBuilderFactory();
        DefaultModelBuilder builder = factory.newInstance();

        for(Dependency dep : config.getAllDependencies()) {
            this.workQueue.add(new Artifact(dep.getGroup(), dep.getName(), dep.getVersion()));
        }

        HashSet<Artifact> resolvedArtifacts = new HashSet();

        while(!this.workQueue.isEmpty()) {
            Artifact art = this.workQueue.remove(0);
            if (!resolvedArtifacts.contains(art)) {
                resolvedArtifacts.add(art);
                this.processDependencies(project, modelResolver, art.group, art.name, art.version, builder);
            }
        }

        for(Artifact art : resolvedArtifacts) {
            System.out.println(art.group + ":" + art.name + ":" + art.version);
        }

    }

    private ArrayList<File> getPomFiles(Project project, String group, String name, String version) {
        ArrayList<File> ret = new ArrayList();

        try {
            Set<ResolvedArtifact> artifacts = project.getConfigurations().detachedConfiguration(new Dependency[]{project.getDependencies().create(String.format("%s:%s:%s", group, name, version))}).getResolvedConfiguration().getResolvedArtifacts();
            if (!artifacts.isEmpty()) {
                return ret;
            }
        } catch (Exception ex) { // ignore exception if not all artifacts can be directly resolved, we will try with pom resolver
        }

        project.getDependencies().createArtifactResolutionQuery().forModule(group, name, version).withArtifacts(JvmLibrary.class, new Class[]{SourcesArtifact.class, JavadocArtifact.class}).execute();
        ArtifactResolutionResult result = project.getDependencies().createArtifactResolutionQuery().forModule(group, name, version).withArtifacts(MavenModule.class, new Class[]{MavenPomArtifact.class}).execute();

        for(ComponentArtifactsResult component : result.getResolvedComponents()) {
            if (component.getId() instanceof ModuleComponentIdentifier) {
                for(ArtifactResult artifact : component.getArtifacts(MavenPomArtifact.class)) {
                    File pomFile = ((ResolvedArtifactResult)artifact).getFile();
                    ret.add(pomFile);
                }
            }
        }

        return ret;
    }

    private void processDependencies(Project project, ModelResolver modelResolver, String group, String name, String version, DefaultModelBuilder builder) {
        for(File pomFile : this.getPomFiles(project, group, name, version)) {
            try {
                ModelBuildingRequest req = new DefaultModelBuildingRequest();
                req.setModelResolver(modelResolver);
                req.setPomFile(pomFile);
                req.getSystemProperties().putAll(System.getProperties());
                req.setValidationLevel(0);
                Model mavenModel = builder.build(req).getEffectiveModel();
                if (mavenModel != null) {
                    StringSubstitutor replacer = createPropertyReplacer(mavenModel);
                    if (mavenModel.getDependencies() != null) {
                        for(org.apache.maven.model.Dependency mavenDep : mavenModel.getDependencies()) {
                            this.addToQueue(mavenDep, replacer);
                        }
                    }

                    if (mavenModel.getDependencyManagement() != null && mavenModel.getDependencyManagement().getDependencies() != null) {
                        for(org.apache.maven.model.Dependency mavenDep : mavenModel.getDependencyManagement().getDependencies()) {
                            this.addToQueue(mavenDep, replacer);
                        }
                    }
                }
            } catch (ModelBuildingException mbe) {
                System.out.println("Unable to process " + String.valueOf(pomFile));
                System.out.println(mbe.getMessage());
            }
        }

    }

    private static @NotNull StringSubstitutor createPropertyReplacer(Model mavenModel) {
        HashMap<String, String> replacements = new HashMap();

        for(String propertyName : mavenModel.getProperties().stringPropertyNames()) {
            replacements.put(propertyName, mavenModel.getProperties().getProperty(propertyName));
        }

        return new StringSubstitutor(replacements);
    }

    private void addToQueue(org.apache.maven.model.Dependency mavenDep, StringSubstitutor replace) {
        this.workQueue.add(new Artifact(replace.replace(mavenDep.getGroupId()), replace.replace(mavenDep.getArtifactId()), replace.replace(mavenDep.getVersion())));
    }

    private static record Artifact(String group, String name, String version) {
        private Artifact(String group, String name, String version) {
            this.group = group;
            this.name = name;
            this.version = version;
        }

        public String group() {
            return this.group;
        }

        public String name() {
            return this.name;
        }

        public String version() {
            return this.version;
        }
    }

    class PomResolver implements ModelResolver {
        private final Project project;

        public PomResolver(Project project) {
            this.project = project;
        }

        public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
            Configuration pomConfiguration = this.project.getConfigurations().detachedConfiguration(new Dependency[]{this.project.getDependencies().create(String.format("%s:%s:%s@pom", groupId, artifactId, version))});
            final File pomXml = pomConfiguration.getSingleFile();
            return new ModelSource() {
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(pomXml);
                }

                public String getLocation() {
                    return pomXml.getAbsolutePath();
                }
            };
        }

        public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
            return this.resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
        }

        public ModelSource resolveModel(org.apache.maven.model.Dependency dependency) throws UnresolvableModelException {
            return this.resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
        }

        public void addRepository(Repository repository) throws InvalidRepositoryException {
            this.addRepository(repository, false);
        }

        public void addRepository(final Repository repository, boolean replace) throws InvalidRepositoryException {
            try {
                if (repository.getUrl().startsWith("http:")) {
                    return;
                }

                URI repoUrl = URI.create(repository.getUrl());
                PrintStream printStream = System.err;
                String repositoryName = repository.getName();
                printStream.println("Add repository " + repositoryName + " " + repository.getUrl());
                if (replace && repository.getName() != null) {
                    System.err.println("Replaced " + repository.getUrl());
                    ArtifactRepository repo = (ArtifactRepository)this.project.getRepositories().findByName(repository.getName());
                    if (repo instanceof MavenArtifactRepository) {
                        ((MavenArtifactRepository)repo).setUrl(repository.getUrl());
                    }

                    return;
                }

                boolean hasRepo = this.project.getRepositories().stream().filter((x) -> {
                    if (x instanceof MavenArtifactRepository m) {
                        if (m.getUrl().equals(repoUrl)) {
                            return true;
                        }
                    }

                    return false;
                }).count() > 0L;
                if (hasRepo) {
                    return;
                }

                System.err.println("Add new " + repository.getUrl());
                this.project.getRepositories().maven(new Action<MavenArtifactRepository>() {
                    public void execute(MavenArtifactRepository mavenArtifactRepository) {
                        mavenArtifactRepository.setName(repository.getName());
                        mavenArtifactRepository.setUrl(repository.getUrl());
                    }
                });
            } catch (IllegalArgumentException var5) {
            }

        }

        public ModelResolver newCopy() {
            return this;
        }
    }
}
