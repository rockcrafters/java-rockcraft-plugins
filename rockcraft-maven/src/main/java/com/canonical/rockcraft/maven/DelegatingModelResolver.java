package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.util.MavenArtifactCopy;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelegatingModelResolver implements ModelResolver {

    private final ModelResolver resolver;
    private final MavenArtifactCopy artifactCopy;

    public DelegatingModelResolver(ModelResolver other, MavenArtifactCopy copy) {
        this.resolver = other;
        this.artifactCopy = copy;
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
        ModelSource source =  resolver.resolveModel(groupId, artifactId, version);
        return storeModelSource(groupId, artifactId, version, source);
    }


    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return storeModelSource(parent.getGroupId(), parent.getArtifactId(), parent.getVersion(), resolver.resolveModel(parent));
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        return storeModelSource(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), resolver.resolveModel(dependency));
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
        resolver.addRepository(repository);
    }

    @Override
    public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
        resolver.addRepository(repository, replace);
    }

    private ModelSource storeModelSource(String groupId, String artifactId, String version, ModelSource source) {
        String location = source.getLocation();
        try {
            InputStream is = source.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (is.available() > 0) {
                bos.write(is.read());
            }
            byte[] data = bos.toByteArray();
            artifactCopy.writePomToMavenRepository(data, groupId, artifactId, version);
            return new ModelSource() {
                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(data);
                }

                @Override
                public String getLocation() {
                    return location;
                }
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ModelResolver newCopy() {
        return new DelegatingModelResolver(resolver, artifactCopy);
    }
}
