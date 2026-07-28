package com.canonical.rockcraft.gradle.dependencies;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BomResolutionListener implements DependencyResolutionListener {
    private final Set<ComponentIdentifier> boms = ConcurrentHashMap.newKeySet();

    @Override
    public void beforeResolve(ResolvableDependencies dependencies) {
        for (Dependency dep : dependencies.getDependencies()) {
            if (!(dep instanceof ExternalModuleDependency)) continue;
            for (DependencyArtifact artifact : ((ExternalModuleDependency) dep).getArtifacts()) {
                if (!"pom".equals(artifact.getType()))
                    continue;
                boms.add(DefaultModuleComponentIdentifier.newId(
                        DefaultModuleIdentifier.newId(dep.getGroup(), dep.getName()),
                        dep.getVersion()));
            }
        }
    }

    @Override
    public void afterResolve(ResolvableDependencies dependencies) { }

    public Set<ComponentIdentifier> getBoms() { return boms; }
}
