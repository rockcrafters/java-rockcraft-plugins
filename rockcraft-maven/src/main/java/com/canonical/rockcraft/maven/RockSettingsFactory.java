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

import com.canonical.rockcraft.builder.RockProjectSettings;
import org.apache.maven.project.MavenProject;

public class RockSettingsFactory {
    public static final RockProjectSettings createRockProjectSettings(MavenProject project) {
        var outputPath = project.getArtifact().getFile().getParentFile().toPath();
        return new RockProjectSettings("maven", project.getName(),
                project.getVersion(), project.getBasedir().getAbsoluteFile().toPath(),
                outputPath);
    }
}
