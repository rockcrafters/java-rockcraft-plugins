/*
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
package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.RockCrafter;
import com.canonical.rockcraft.builder.RockcraftOptions;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.toolchain.JavaToolchainService;


import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This task writes <i>rockcraft.yaml</i> file for the application.
 */
public abstract class CreateRockcraftTask extends AbstractRockcraftTask {

    @Inject
    protected abstract JavaToolchainService getToolchainService();

    /**
     * Constructs CreateRockcraftTask
     *
     * @param options - rockcraft options
     */
    @Inject
    public CreateRockcraftTask(RockcraftOptions options) {
        super(options);
    }

    /**
     * Task action to write <i>rockcraft.yaml</i>
     */
    @SuppressWarnings("unchecked")
    @TaskAction
    public void writeRockcraft() {
        if ("".equals(getOptions().getBuildPackage())) {
            getOptions().setBuildPackage(Toolchain.getToolchainPackage(getProject(), getToolchainService(), getLogger()));
        }
        HashSet<File> artifacts = new HashSet<>();

        if (getOptions().isNativeImage()) {
            Task nativeCompileTask = getProject().getTasks().getByName("nativeCompile");
            File nativeDir = nativeCompileTask.getOutputs().
                    getFiles().getFiles().
                    stream().
                    findFirst().
                    get();
            String nativeImageName = getProject().getRootProject().getName();
            artifacts.add(new File(nativeDir, nativeImageName));
        } else {
            Set<Object> dependsOn = getDependsOn();
            for (Object entry : dependsOn) {
                HashSet<Task> tasks = (HashSet<Task>) entry;
                for (Task task : tasks) {
                    artifacts.addAll(task.getOutputs().getFiles().getFiles());
                }
            }
        }

        try {
            RockCrafter crafter = new RockCrafter(RockSettingsFactory.createRockProjectSettings(getProject()),
                 getOptions(), new ArrayList<>(artifacts));
            crafter.writeRockcraft();
        } catch (IOException e) {
            throw new UnsupportedOperationException("Failed to write rockcraft.yaml: " + e.getMessage());
        }
    }
}
