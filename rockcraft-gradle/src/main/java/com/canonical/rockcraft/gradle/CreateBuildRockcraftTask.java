/*
 * Copyright 2025 Canonical Ltd.
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

import com.canonical.rockcraft.builder.BuildRockCrafter;
import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public abstract class CreateBuildRockcraftTask extends DefaultTask {

    private final BuildRockcraftOptions options;

    @Inject
    public CreateBuildRockcraftTask(BuildRockcraftOptions options) {
        super();
        this.options = options;
    }

    @TaskAction
    @SuppressWarnings("unchecked")
    public void writeRockcraft() throws IOException {
        HashSet<File> artifacts = new HashSet<File>();
        Set<Object> dependsOn = getDependsOn();
        for (Object entry : dependsOn) {
            HashSet<Task> tasks = (HashSet<Task>) entry;
            for (Task task : tasks) {
                artifacts.addAll(task.getOutputs().getFiles().getFiles());
            }
        }
        BuildRockCrafter crafter = new BuildRockCrafter(RockSettingsFactory.createBuildRockProjectSettings(getProject()),
                options, new ArrayList<>(artifacts));
        crafter.writeRockcraft();
    }
}
