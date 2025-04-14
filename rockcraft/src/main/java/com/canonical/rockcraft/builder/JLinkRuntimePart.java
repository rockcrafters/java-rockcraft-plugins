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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Implements Rockcraft Java runtime part using JLink plugin (experimental)
 */
public class JLinkRuntimePart implements IRuntimeProvider {

    private final RockcraftOptions options;

    /**
     * Constructs the JLinkRuntimePart
     * @param options - plugin options
     */
    public JLinkRuntimePart(RockcraftOptions options) {
        this.options = options;
    }

    /**
     * Retuns the part contents
     * @param files - list of jar files to analyze
     * @return part contents
     */
    @Override
    public Map<String, Object> getRuntimePart(List<File> files) {
        ArrayList<String> relativeJars = new ArrayList<String>();
        for (File jar : files) {
            relativeJars.add(String.format("jars/%s", jar.getName()));
        }
        HashMap<String, Object> part = new HashMap<String, Object>();
        part.put("build-packages", new String[]{options.getBuildPackage()});
        part.put("after", new String[]{"gradle/rockcraft/dump", "gradle/rockcraft/deps"});
        part.put("plugin", "jlink");
        part.put("jlink-jars", relativeJars);
        return part;
    }
}
