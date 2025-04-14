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
 * Uses a verbose list of commands to generate Java runtime
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

    private void append(StringBuilder buffer, String str) {
        buffer.append(str);
        buffer.append("\n");
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
        part.put("build-packages", new String[]{options.getBuildPackage()});

        StringBuilder jarList = new StringBuilder();
        for (File jar : files) {
            if (jarList.length() > 0)
                jarList.append(" ");
            jarList.append(String.format("${CRAFT_STAGE}/jars/%s", jar.getName()));
        }

        StringBuilder commands = new StringBuilder();
        append(commands, "JAVA_HOME=$(dirname $(dirname $(readlink -f /usr/bin/java)))");
        append(commands, "JAVA_HOME=${JAVA_HOME:1}");
        append(commands, "PROCESS_JARS=\"" + jarList + "\"");
        append(commands, "mkdir -p ${CRAFT_PART_BUILD}/tmp");
        append(commands,
                "(cd ${CRAFT_PART_BUILD}/tmp && for jar in ${PROCESS_JARS}; do jar xvf ${jar}; done;)"
        );
        append(commands, "CPATH=");
        append(commands, "for file in $(find \"${CRAFT_PART_BUILD}/tmp\" -type f -name \"*.jar\"); do");
        append(commands, "  CPATH=\"$CPATH:${file}\"");
        append(commands, "done");
        append(commands, "for file in $(find \"${CRAFT_STAGE}\" -type f -name \"*.jar\"); do");
        append(commands, "  CPATH=\"$CPATH:${file}\"");
        append(commands, "done");
        append(commands, "echo ${CPATH}");
        append(commands, "if [ \"x${PROCESS_JARS}\" != \"x\" ]; then");
        append(commands, "  deps=$(jdeps --print-module-deps -q --recursive --ignore-missing-deps \\");
        append(commands, String.format("  --multi-release %d --class-path=${CPATH} ${PROCESS_JARS}); else deps=java.base; fi", options.getTargetRelease()));
        append(commands, "INSTALL_ROOT=${CRAFT_PART_INSTALL}/${JAVA_HOME}");
        append(commands,
                "rm -rf ${INSTALL_ROOT} && jlink --add-modules ${deps} --output ${INSTALL_ROOT}"
        );

        append(commands,
                "(cd ${CRAFT_PART_INSTALL} && mkdir -p usr/bin && ln -s --relative ${JAVA_HOME}/bin/java usr/bin/)");
        part.put("override-build", commands.toString());
        part.put("after", new String[]{"gradle/rockcraft/dump", "gradle/rockcraft/deps"});
        return part;
    }
}
