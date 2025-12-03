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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses chisel slices to generate the java runtime. Default for Java 8
 */
public class RawRuntimePart implements IRuntimeProvider {

    private final RockcraftOptions options;

    /**
     * Constructs the RawRuntimePart
     * @param options - plugin options
     */
    public RawRuntimePart(RockcraftOptions options) {
        this.options = options;
    }

    /**
     * Generate code to create Java runtime image
     * @param files - list of jar files to analyze
     * @return part content
     */
    @Override
    public Map<String, Object> getRuntimePart(List<File> files) {
        HashMap<String, Object> part = new HashMap<String, Object>();
        part.put("plugin", "nil");
        String jrePackage = options.getBuildPackage().replace("-jdk", "-jre");

        if (options.isJava8()) {
            part.put("stage-packages", new String[] {
                    "openjdk-8-jre-headless_core",
                    "openjdk-8-jre-headless_locale",
                    "openjdk-8-jre-headless_security",
                    "openjdk-8-jre-headless_management",
                    "openjdk-8-jre-headless_jfr",
                    "openjdk-8-jre-headless_tools",
                    "openjdk-8-jre-headless_jplis",
                    "openjdk-8-jre-headless_jndidns",
                    "openjdk-8-jre-headless_zipfs",
                    "openjdk-8-jre-headless_sctp",
            });
        } else {
            part.put("stage-packages", new String[] {
                    options.getBuildPackage() + "_standard",
            });
        }
        part.put("after", new String[]{"gradle/rockcraft/dump", "gradle/rockcraft/deps"});
        return part;
    }
}
