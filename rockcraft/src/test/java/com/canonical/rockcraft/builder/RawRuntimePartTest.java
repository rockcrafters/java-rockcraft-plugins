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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RawRuntimePartTest {

    private ArrayList<File> input;

    @BeforeEach
    void setUp() {
        input = new ArrayList<File>();
        input.add(new File("/tmpfoo.jar"));
    }

    @Test
    void rawRuntimePart() {
        RockcraftOptions options = new RockcraftOptions();
        options.setBuildPackage("openjdk-21-jdk");
        RawRuntimePart part = new RawRuntimePart(options);
        Map<String, Object> code = part.getRuntimePart(input);
        assertEquals("nil", code.get("plugin"));
        String[] ret = (String[]) code.get("build-packages");
        assertEquals("openjdk-21-jdk", ret[0]);
        assertTrue(code.get("override-build").toString().contains("--multi-release 21"));
    }

    @Test
    void rawRuntimeCustomOpenjdk() {
        RockcraftOptions options = new RockcraftOptions();
        options.setTargetRelease(8);
        options.setBuildPackage("openjdk-11-jdk");
        RawRuntimePart part = new RawRuntimePart(options);
        Map<String, Object> code = part.getRuntimePart(input);
        String[] ret = (String[]) code.get("build-packages");
        assertEquals("openjdk-11-jdk", ret[0]);
        assertTrue(code.get("override-build").toString().contains("--multi-release 8"));
    }

    @Test
    void rawRuntimeMultipleJars() {
        RockcraftOptions options = new RockcraftOptions();
        options.setJlink(false);
        RawRuntimePart part = new RawRuntimePart(options);
        Map<String, Object> code = part.getRuntimePart(input);
        // classpath should contain both embeeded jars from the boot jar and every jar found in the jar directory
        // to avoid building incomplete runtime if the dependencies are packaged separately
        assertTrue(code.get("override-build").toString().contains("for file in $(find \"${CRAFT_PART_BUILD}/tmp\" -type f -name \"*.jar\"); do"));
        assertTrue(code.get("override-build").toString().contains("for file in $(find \"${CRAFT_STAGE}\" -type f -name \"*.jar\"); do"));
    }
}
