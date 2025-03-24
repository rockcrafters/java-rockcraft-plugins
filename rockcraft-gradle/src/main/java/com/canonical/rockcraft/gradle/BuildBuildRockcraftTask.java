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

import com.canonical.rockcraft.builder.CommonRockcraftOptions;
import com.canonical.rockcraft.builder.RockBuilder;
import com.canonical.rockcraft.builder.RockProjectSettings;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Gradle task to pack build/test rock
 */
public class BuildBuildRockcraftTask extends DefaultTask {
    private final BuildRockcraftOptions options;

    /**
     * Construct new BuildBuildRockcraftTask
     * @param options - rockcraft project options
     */
    @Inject
    public BuildBuildRockcraftTask(BuildRockcraftOptions options) {
        this.options = options;
    }

    /**
     * Pack the build rock
     * @throws InterruptedException - rockcraft process was interrupted
     * @throws IOException - rockcraft process failed to pack the rock
     */
    @TaskAction
    public void buildBuildRock() throws InterruptedException, IOException {
        RockProjectSettings settings = RockSettingsFactory.createBuildRockProjectSettings(getProject());
        RockBuilder.buildRock(settings, options);
    }
}
