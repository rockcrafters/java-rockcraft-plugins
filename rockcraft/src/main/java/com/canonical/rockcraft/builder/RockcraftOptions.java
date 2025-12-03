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

/**
 * Rockcraft.yaml generation options
 */
public class RockcraftOptions extends CommonRockcraftOptions {

    private int targetRelease = 21;
    private boolean jlink = true;
    private String command = "";
    private boolean createService = true;
    private String distTask;
    private boolean nativeImage = false;

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
     * Gets a flag whether to use jlink plugin (early access option).
     * Default - false.
     *
     * @return jlink plugin flag
     */
    public boolean getJlink() {
        return jlink && !isJava8();
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
     * Get deployment task/goal
     * @return deployment task after which the create-rock should be executed
     */
    public String getDistTask() {
        return distTask;
    }

    /**
     * Set deployment task/goal
     * @param distTask - deployment task/goal, e.g. jar
     */
    public void setDistTask(String distTask) {
        this.distTask = distTask;
    }

     /* Get whether this is a native-image request
     *
     * @return default false
     */
    public boolean isNativeImage() {
        return nativeImage;
    }

    /**
     * Enable or disable native image creation
     *
     * @param nativeImage - whether to create a native image
     */
    public void setNativeImage(boolean nativeImage) {
        this.nativeImage = nativeImage;
    }
}
