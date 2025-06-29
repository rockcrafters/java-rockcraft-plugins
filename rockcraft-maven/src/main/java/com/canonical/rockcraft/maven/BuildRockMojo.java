/**
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
package com.canonical.rockcraft.maven;

import com.canonical.rockcraft.builder.IRockcraftNames;
import com.canonical.rockcraft.builder.RockBuilder;
import com.canonical.rockcraft.builder.RockProjectSettings;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;

/**
 * Builds rock image
 */
@Mojo(name = "build-rock", threadSafe = false, requiresProject = true, defaultPhase = LifecyclePhase.INSTALL)
public class BuildRockMojo extends AbstractRockMojo {

    /**
     * No specific initialization
     */
    public BuildRockMojo(){}

    /**
     * Executes mojo to build the rock image
     */
    public void execute() throws MojoExecutionException {
        super.execute();
        try {
            RockProjectSettings settings = RockSettingsFactory.createRockProjectSettings(getRuntimeInformation(), getProject());
            if (!settings.getRockOutput().resolve(IRockcraftNames.ROCKCRAFT_YAML).toFile().exists()) {
                getLog().warn("Skipping build-rock, rockcraft.yaml does not exist");
                return;
            }
            RockBuilder.buildRock(settings, getOptions());
        } catch (InterruptedException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }
}
