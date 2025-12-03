/**
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
package com.canonical.rockcraft.builder;

import com.canonical.rockcraft.util.ToolchainHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides common options for rockcraft.yaml generation
 */
public class CommonRockcraftOptions {
    private String buildPackage = "";
    private String summary = "";
    private Path description = null;
    private String source;
    private String branch;
    private RockArchitecture[] architectures = new RockArchitecture[0];
    private List<String> slices = new ArrayList<String>();
    private Path rockcraftYaml = null;

    /**
     * Get the Ubuntu OpenJDK package used to build the runtime image
     *
     * @return package name
     */
    public String getBuildPackage() {
        return buildPackage;
    }

    /**
     * Set the OpenJDK package used to build the runtime image
     *
     * @param buildPackage - Ubuntu package name
     */
    public void setBuildPackage(String buildPackage) {
        this.buildPackage = buildPackage;
    }

    /**
     * Gets <i>chisel-releases</i> branch
     *
     * @return <i>chisel-releases</i> branch
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Sets <i>chisel-releases</i> branch
     *
     * @param branch - git branch
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Gets the summary comment for the ROCK
     *
     * @return summary comment
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary comment for the ROCK
     *
     * @param summary - summary comment
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Gets a description file for the ROCK
     *
     * @return path to the description
     */
    public Path getDescription() {
        return description;
    }

    /**
     * Sets the path to the description file
     *
     * @param description - description file
     */
    public void setDescription(String description) {
        if (description != null) {
            this.description = Paths.get(description);
        }
    }

    /**
     * Gets the list of the supported architectures
     *
     * @return supported architectures
     */
    public RockArchitecture[] getArchitectures() {
        return architectures;
    }

    /**
     * Sets the list of the supported architectures
     *
     * @param architectures - supported architectures
     */
    public void setArchitectures(RockArchitecture[] architectures) {
        this.architectures = architectures;
    }

    /**
     * Get chisel slices to install
     *
     * @return list of the slice names
     */
    public List<String> getSlices() {
        return slices;
    }

    /**
     * Overrides chisel slices to install
     *
     * @param slices - list of chisel slices
     */
    public void setSlices(List<String> slices) {
        this.slices = slices;
    }

    /**
     * Get Git repository URL of the <i>chisel-releases</i>
     *
     * @return Git repository URL
     */
    public String getSource() {
        return source;
    }

    /**
     * Override <i>chisel-releases</i> repository URL
     *
     * @param source - Git repository URL
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get path to the optional rockcraft.yaml
     *
     * @return path to rockcraft yaml
     */
    public Path getRockcraftYaml() {
        return rockcraftYaml;
    }

    /**
     * Sets the path to rockcraft.yaml file
     *
     * @param rockcraftYaml - rockcraft yaml file
     */
    public void setRockcraftYaml(String rockcraftYaml) {
        if (rockcraftYaml != null) {
            this.rockcraftYaml = Paths.get(rockcraftYaml);
        }
    }

    public boolean isJava8() {
        return ToolchainHelper.OPENJDK_8.equals(getBuildPackage()) ||ToolchainHelper.OPENJDK_8_HEADLESS.equals(getBuildPackage());
    }
}
