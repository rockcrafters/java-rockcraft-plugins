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
 * Pushes rock image to the local docker daemon
 */
@Mojo(name = "push-rock", threadSafe = false, requiresProject = true, defaultPhase = LifecyclePhase.INSTALL)
public class PushRockMojo extends AbstractRockMojo {

    /**
     * No specific initialization
     */
    public PushRockMojo(){}

    /**
     * Executes mojo: pushes existing rock file to the docker daemon
     */
    public void execute() throws MojoExecutionException {
        super.execute();
        try {
            RockProjectSettings settings = RockSettingsFactory.createRockProjectSettings(getRuntimeInformation(), getProject());
            if (!settings.getRockOutput().resolve(IRockcraftNames.ROCKCRAFT_YAML).toFile().exists()) {
                getLog().warn("Skipping push-rock, rockcraft.yaml does not exist");
                return;
            }
            RockBuilder.pushRock(settings, getOptions());
        } catch (InterruptedException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
