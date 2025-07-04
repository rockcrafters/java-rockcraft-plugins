package com.canonical.rockcraft.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.util.List;

public class POMLookup {
    public static File lookup(Artifact art, List<RemoteRepository> repositories, RepositorySystemSession repoSession, RepositorySystem repoSystem) throws MojoExecutionException {
        ArtifactRequest req = new ArtifactRequest();
        req.setArtifact(new DefaultArtifact(art.getGroupId(), art.getArtifactId(), "pom", art.getVersion()));
        req.setRepositories(repositories);
        ArtifactResult result = null;
        try {
            result = repoSystem.resolveArtifact(repoSession, req);
            return result.getArtifact().getFile();
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e);
        }
    }
}
