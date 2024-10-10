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
package com.canonical.rockcraft.builder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Rockcraft.yaml generation options
 */
public class RockcraftOptions {

    private String buildPackage = "openjdk-21-jdk";
    private int targetRelease = 21;
    private boolean jlink = false;
    private String summary = "";
    private Path description = null;
    private String command = "";
    private String source;
    private String branch;
    private RockArchitecture[] architectures = new RockArchitecture[0];
    private List<String> slices = new ArrayList<String>();
    private Path rockcraftYaml = null;
    private boolean createService = true;

    /**
     * Construct RockcraftOptions
     */
    public RockcraftOptions() {
    }

    /**
     * Gets the target release (integer)
     *
     * @return target release
     */
    public int getTargetRelease() {
        return targetRelease;
    }

    /**
     * Sets the target release
     *
     * @param targetRelease - target OpenJDK release
     */
    public void setTargetRelease(int targetRelease) {
        this.targetRelease = targetRelease;
    }

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
     * Gets a flag whether to use jlink plugin (early access option).
     * Default - false.
     *
     * @return jlink plugin flag
     */
    public boolean getJlink() {
        return jlink;
    }

    /**
     * Enable/Disable jlink plugin
     *
     * @param jlink - flag
     */
    public void setJlink(boolean jlink) {
        this.jlink = jlink;
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
     * Gets the service command (opitonal, override)
     *
     * @return the service command line
     */
    public String getCommand() {
        return command;
    }

    /**
     * Override the service command line
     *
     * @param command - command line
     */
    public void setCommand(String command) {
        this.command = command;
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
     * @param description - description file
     */
    public void setRockcraftYaml(String rockcraftYaml) {
        if (rockcraftYaml != null) {
            this.rockcraftYaml = Paths.get(rockcraftYaml);
        }
    }

    /**
     * Get whether to create service section
     *
     * @return default true
     */
    public boolean isCreateService() {
        return createService;
    }

    /**
     * Enable or disable service creation
     *
     * @param createService - whether to crreate service section
     */
    public void setCreateService(boolean createService) {
        this.createService = createService;
    }

    /**
     * The list of supported Ubuntu architectures
     */
    public enum RockArchitecture {
        /**
         * AMD64
         */
        amd64,
        /**
         * ARM64
         */
        arm64,
        /**
         * ARM hard float
         */
        armhf,
        /**
         * I386
         */
        i386,
        /**
         * PowerPC 64 EL
         */
        ppc64el,
        /**
         * RISCV
         */
        riscv64,
        /**
         * S390X
         */
        s390x
    }
}
