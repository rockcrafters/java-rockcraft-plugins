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
package com.canonical.rockcraft.gradle;

/**
 * Plugin task names
 */
public interface ITaskNames {
    /**
     * Jar task - used to package application jar by 'application' task
     */
    String JAR = "jar";
    /**
     * bootJar - used to package String Boot jar by spring boot plugin
     */
    String BOOT_JAR = "bootJar";
    /**
     * jlink task is provided by Beryx Jlink plugin
     */
    String JLINK = "jlink";

    /***
     * runtime task is provided by Beryx Runtime plugin
     */
    String RUNTIME = "runtime";

    /**
     * dependencies-export - used to write dependencies of the project
     */
    String DEPENDENCIES = "dependencies-export";

    /**
     * create-rock - generates rockcraft.yaml for the application rock
     */
    String CREATE_ROCK = "create-rock";
    /**
     * build-rock - packs the application rock
     */
    String BUILD_ROCK = "build-rock";
    /**
     * push-rock - pushes the application rock to a container registry
     */
    String PUSH_ROCK = "push-rock";
    /**
     *  create-build-rock - generates rockcraft.yaml for the build rock
     */
    String CREATE_BUILD_ROCK = "create-build-rock";
    /**
     *  build-build-rock - packs the build rock
     */
    String BUILD_BUILD_ROCK = "build-build-rock";
    /**
     * push-build-rock - pushes the build rock to a container registry
     */
    String PUSH_BUILD_ROCK = "push-build-rock";
    /**
     * checkRockcraft - verifies that rockcraft is installed
     */
    String CHECK_ROCKCRAFT = "checkRockcraft";
}
