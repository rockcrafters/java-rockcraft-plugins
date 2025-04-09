/*
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.canonical.rockcraft.util.MapMerger;

/**
 * Creates build rock rockcraft.yaml
 */
public class BuildRockCrafter extends AbstractRockCrafter {

    /**
     * Create build rock RockCrafter
     * @param settings - project settings
     * @param options - project options
     * @param artifacts - list of artifact directories containing project dependencies
     */
    public BuildRockCrafter(RockProjectSettings settings, BuildRockcraftOptions options, List<File> artifacts) {
        super(settings, options, artifacts);
    }

    /**
     * Create rockcraft.yaml file
     *
     * @param root - rockcraft.yaml's directory
     * @param files - directories containing dependencies
     * @return rockcraft.yaml string
     * @throws IOException - failed to generate rockcraft.yaml
     */
    @Override
    protected String createRockcraft(Path root, List<File> files) throws IOException {
        if (files.size() != 1){
            throw new UnsupportedOperationException("Build rock requires a single file input - a directory with a maven repository of dependencies");
        }
        BuildRockcraftOptions buildOptions = (BuildRockcraftOptions) getOptions();
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        Map<String, Object> rockcraft = MapMerger.merge(createCommonSection(), loadRockcraftSnippet(yaml));

        StringBuilder yamlOutput = new StringBuilder();
        yamlOutput.append(yaml.dump(rockcraft));
        yamlOutput.append("\n");
        rockcraft.clear();

        rockcraft.put("parts", createParts(getSettings(), buildOptions, files));
        yamlOutput.append(yaml.dump(rockcraft));
        rockcraft.clear();

        return yamlOutput.toString();
    }

    private Map<String, Object> createParts(RockProjectSettings settings, BuildRockcraftOptions options, List<File> files) {
        Map<String,Object> parts = new HashMap<>();
        parts.put("dependencies", createDependenciesPart());
        parts.put("maven-cache", createMavenRepository(settings, options, files));
        parts.put("build-tool", createBuildTool(settings, options));
        return parts;
    }

    private Map<String, Object> createBuildTool(RockProjectSettings settings, BuildRockcraftOptions options) {
        Map<String,Object> part = new HashMap<>();
        part.put("plugin", "nil");
        if (settings.getBuildSystem() == BuildSystem.maven) {
            part.put("stage-packages", new String[] {"maven"});
            part.put("stage", new String[]{
                    "usr/share/maven",
                    "usr/share/java",
            });
        }
        else if (settings.getBuildSystem() == BuildSystem.gradle) {
            part.put("build-packages", new String[] {"unzip", "wget"});
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("wget https://services.gradle.org/distributions/gradle-%s-bin.zip\n", settings.getBuildSystemVersion()));
            sb.append(String.format("unzip -o -qq gradle-%s-bin.zip\n", settings.getBuildSystemVersion()));
            sb.append("mkdir -p $CRAFT_PART_INSTALL/usr/share/gradle\n");
            sb.append("mkdir -p $CRAFT_PART_INSTALL/usr/bin\n");
            sb.append("rm -rf $CRAFT_PART_INSTALL/usr/share/gradle/*\n");
            sb.append(String.format("mv gradle-%s/* $CRAFT_PART_INSTALL/usr/share/gradle/\n", settings.getBuildSystemVersion()));
            sb.append("cd $CRAFT_PART_INSTALL/ && ln -s --relative usr/share/gradle/bin/gradle usr/bin/\n");
            sb.append("craftctl default\n");
            part.put("override-build", sb.toString());
        }
        return part;
    }

    private Map<String, Object> createMavenRepository(RockProjectSettings settings, BuildRockcraftOptions options, List<File> files) {
        Map<String,Object> part = new HashMap<>();
        part.put("plugin", "nil");
        String source = settings.getRockOutput().relativize(files.get(0).toPath()).toString();
        part.put("source", source);
        part.put("source-type", "local");
        StringBuilder commands = new StringBuilder();
        commands.append("mkdir -p ${CRAFT_PART_INSTALL}/var/lib/pebble/default/.m2/repository/\n");
        commands.append("cp -r * ${CRAFT_PART_INSTALL}/var/lib/pebble/default/.m2/repository/\n");
        commands.append("# workaround https://github.com/canonical/craft-parts/issues/507\n");
        commands.append("chown -R 584792:584792  ${CRAFT_PART_INSTALL}/var/lib/pebble/default\n");
        commands.append("craftctl default");
        part.put("override-build", commands.toString());

        HashMap<String, Object> permissions = new HashMap<>();
        permissions.put("path", "/var/lib/pebble/default");
        permissions.put("owner", 584792);
        permissions.put("group", 584792);
        permissions.put("mode", "755");
        part.put("permissions", new HashMap[] { permissions });
        return part;
    }

    private Map<String, Object> createDependenciesPart() {
        Map<String,Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("build-packages", new String[] {"busybox", options.getBuildPackage()});

        List<String> slices = getOptions().getSlices();
        slices.add("busybox_bins");
        slices.add("base-files_base");
        slices.add("base-files_chisel");
        slices.add("git_bins");
        slices.add("git_http-support");
        slices.add(options.getBuildPackage() + "_standard");
        slices.add(options.getBuildPackage() + "_headers");
        slices.add(options.getBuildPackage() + "_debug-headers");

        if (getOptions().getSource() != null) {
            part.put("source", getOptions().getSource());
            part.put("source-type", "git");
        }
        if (getOptions().getBranch() != null) {
            part.put("source-branch", getOptions().getBranch());
        }

        StringBuilder overrideCommands = new StringBuilder();
        overrideCommands.append("chisel cut ");
        if (getOptions().getSource() != null) {
            overrideCommands.append("--release ./ ");
        }
        overrideCommands.append(" --root ${CRAFT_PART_INSTALL}/ \\\n");
        for (String slice : slices) {
            overrideCommands.append("  ");
            overrideCommands.append(slice);
            overrideCommands.append(" \\\n");
        }
        overrideCommands.append("\n");
        overrideCommands.append("busybox --install -s ${CRAFT_PART_INSTALL}/usr/bin/\n");
        overrideCommands.append("cd ${CRAFT_PART_INSTALL} && PATH=/usr/bin find . -type f -name java -exec ln -sf --relative {} ${CRAFT_PART_INSTALL}/usr/bin/ \\;\n");
        overrideCommands.append("mkdir -p ${CRAFT_PART_INSTALL}/etc/ssl/certs/java/ &&  cp /etc/ssl/certs/java/cacerts ${CRAFT_PART_INSTALL}/etc/ssl/certs/java/cacerts");

        overrideCommands.append("\ncraftctl default\n");
        part.put("override-build", overrideCommands.toString());
        return part;
    }
}
