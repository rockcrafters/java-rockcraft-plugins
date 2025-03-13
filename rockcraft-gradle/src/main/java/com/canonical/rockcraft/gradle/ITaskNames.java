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

/**
 * Task name constants.
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
}
