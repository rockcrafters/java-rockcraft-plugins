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

import com.canonical.rockcraft.util.ToolchainHelper;
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
    void rawRuntimeCustomOpenjdk() {
        RockcraftOptions options = new RockcraftOptions();
        options.setTargetRelease(8);
        options.setBuildPackage("openjdk-11-jdk");
        RawRuntimePart part = new RawRuntimePart(options);
        Map<String, Object> code = part.getRuntimePart(input);
        String[] ret = (String[]) code.get("stage-packages");
        assertEquals("openjdk-11-jre-headless_standard", ret[0]);
        assertTrue(code.get("override-build").toString().contains("TOOLS="));
    }

    @Test
    void rawRuntimeOpenJDK8() {
        RockcraftOptions options = new RockcraftOptions();
        options.setJlink(false);
        options.setBuildPackage(ToolchainHelper.OPENJDK_8);
        RawRuntimePart part = new RawRuntimePart(options);
        Map<String, Object> code = part.getRuntimePart(input);
        String[] ret = (String[]) code.get("stage-packages");
        assertEquals("openjdk-8-jre-headless_core", ret[0]);
    }
}
