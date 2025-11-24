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

import com.canonical.rockcraft.util.MapMerger;
import org.apache.commons.text.StringSubstitutor;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates build rock rockcraft.yaml
 */
public class BuildRockCrafter extends AbstractRockCrafter {

    /**
     * Create build rock RockCrafter
     *
     * @param settings  - project settings
     * @param options   - project options
     * @param artifacts - list of artifact directories containing project dependencies
     */
    public BuildRockCrafter(RockProjectSettings settings, BuildRockcraftOptions options, List<File> artifacts) {
        super(settings, options, artifacts);
    }

    /**
     * Create rockcraft.yaml file
     *
     * @param root  - rockcraft.yaml's directory
     * @param files - directories containing dependencies
     * @return rockcraft.yaml string
     * @throws IOException - failed to generate rockcraft.yaml
     */
    @Override
    protected String createRockcraft(Path root, List<File> files) throws IOException {
        if (files.size() != 1){
            throw new UnsupportedOperationException("Build rock requires a single file input - a directory with a maven repository of dependencies");
        }

        writeResourceFiles();

        BuildRockcraftOptions buildOptions = (BuildRockcraftOptions) getOptions();
        Yaml yaml = YamlFactory.createYaml();

        Map<String, Object> commonSection = createCommonSection();
        String name = (String) commonSection.get("name");
        commonSection.put("name", "build-" + name);

        Map<String, Object> snippetRockcraft = loadRockcraftSnippet(yaml);
        Map<String, Object> snippetParts = (Map<String, Object>) snippetRockcraft.get(PARTS);
        snippetRockcraft.remove(PARTS);

        Map<String, Object> rockcraft = MapMerger.merge(commonSection, snippetRockcraft);
        StringBuilder yamlOutput = new StringBuilder();
        yamlOutput.append(yaml.dump(rockcraft));
        yamlOutput.append("\n");
        rockcraft.clear();

        rockcraft.put(PARTS, MapMerger.merge(createParts(getSettings(), buildOptions, files), snippetParts));
        yamlOutput.append(yaml.dump(rockcraft));
        rockcraft.clear();

        return yamlOutput.toString();
    }

    private Map<String, Object> createParts(RockProjectSettings settings, BuildRockcraftOptions options, List<File> files) {
        Map<String, Object> parts = new HashMap<>();
        parts.put("dependencies", createDependenciesPart());
        parts.put("openjdk", createOpenJDKPart());
        parts.put("maven-cache", createMavenRepository(settings, options, files));
        if (settings.getBuildSystem() == BuildSystem.maven) {
            parts.put("maven-settings", createMavenSettings());
        } else if (settings.getBuildSystem() == BuildSystem.gradle) {
            parts.put("gradle-init", createInitFile());
        } else {
            throw new UnsupportedOperationException("Unknown build system " + settings.getBuildSystem());
        }

        parts.put("build-tool", createBuildTool(settings, options));
        parts.put("entrypoint", createEntrypoint(settings, options));
        if (options.isNativeImage()) {
            parts.put("graalvm-install", createGraalVMInstall(settings, options));
            parts.put("native-compile-deps", createNativeCompileDeps(settings, options));
        }
        if (settings.getBuildSystem() == BuildSystem.gradle) {
            parts.put("gradle-init", createInitFile());
        }
        return parts;
    }

    private Map<String, Object> createMavenSettings() {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("source", ".");
        String overrideBuild = "mkdir -p ${CRAFT_PART_INSTALL}/home/ubuntu/.m2/\n" +
                "cp settings.xml ${CRAFT_PART_INSTALL}/home/ubuntu/.m2/\n" +
                "chown -R 1000:1000 ${CRAFT_PART_INSTALL}/home/ubuntu\n" +
                "craftctl default";
        part.put("override-build", overrideBuild);
        return part;
    }

    private Map<String, Object> createEntrypoint(RockProjectSettings settings, BuildRockcraftOptions options) {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("source", ".");
        StringBuilder overrideBuild = new StringBuilder();

        overrideBuild.append("mkdir -p $CRAFT_PART_INSTALL/usr/bin\n");

        if (settings.getBuildSystem() == BuildSystem.maven) {
            if (options.isNativeImage()) {
                overrideBuild.append("cp build-maven-native.sh $CRAFT_PART_INSTALL/usr/bin/build\n");
            } else {
                overrideBuild.append("cp build-maven.sh $CRAFT_PART_INSTALL/usr/bin/build\n");
            }
        } else if (settings.getBuildSystem() == BuildSystem.gradle) {
            if (options.isNativeImage()) {
               overrideBuild.append("cp build-gradle-native.sh $CRAFT_PART_INSTALL/usr/bin/build\n");
            } else {
                overrideBuild.append("cp build-gradle.sh $CRAFT_PART_INSTALL/usr/bin/build\n");
            }
        } else {
            throw new IllegalArgumentException("Unknown build system");
        }
        overrideBuild.append("chmod +rx $CRAFT_PART_INSTALL/usr/bin/build\n");
        overrideBuild.append("craftctl default\n");
        part.put("override-build", overrideBuild.toString());

        return part;
    }

    private Map<String, Object> createInitFile() {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("source", ".");
        String overrideBuild = "mkdir -p ${CRAFT_PART_INSTALL}/usr/share/gradle/init.d/\n" +
                "cp local-build.gradle ${CRAFT_PART_INSTALL}/usr/share/gradle/init.d/\n" +
                "craftctl default";
        part.put("override-build", overrideBuild);
        return part;
    }

    private void writeResourceFiles() throws IOException {
        for (String resource : new String[]{"build-gradle.sh", "build-maven.sh", "local-build.gradle", "settings.xml", "build-maven-native.sh", "build-gradle-native.sh"}) {
            try (InputStream is = getClass().getResourceAsStream(String.format("/com/canonical/rockcraft/builder/%s", resource))) {
                Path output = getSettings().getRockOutput().resolve(resource);
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                byte[] buffer = new byte[is.available()];
                while (is.available() > 0) {
                    int read = is.read(buffer);
                    if (read > 0) {
                        data.write(buffer, 0, read);
                    }
                }
                data.flush();
                Files.write(output, replaceMacros(data.toString()).getBytes());
            }
        }
    }

    private String replaceMacros(String content) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("gradle-user-home", "export GRADLE_USER_HOME=${WORKDIR}/.gradle");
        replacements.put("goal",
                Arrays.stream(((BuildRockcraftOptions) getOptions()).getBuildGoals()).
                        collect(Collectors.joining(" ")));
        StringSubstitutor substitutor = new StringSubstitutor(replacements, "!!", "!!");
        return substitutor.replace(content);
    }

    private Map<String, Object> createNativeCompileDeps(RockProjectSettings settings, BuildRockcraftOptions options) {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("stage-packages", new String[] { "gcc", "zlib1g-dev" });

       /* Files installed by gcc needed to be explicitly staged because there is an overlap between what
          we stage here and what is staged in the `dependencies` part, using chisel slice definitions.
          Moreover, gcc cannot be staged in `dependencies` because it does not have a chisel slice
          definition. So, we selectively stage what native-image needs.
        */
        part.put("stage", new String[]{
                // compiler
                "usr/bin/*gcc*",

                // assembler
                "usr/bin/*as*",

                // linker
                "usr/bin/*ld*",

                // C runtime libraries
                "usr/libexec/gcc/x86_64-linux-gnu/13/*",
                "usr/lib/x86_64-linux-gnu/*",
                "usr/lib/gcc/x86_64-linux-gnu/13/*",

                // C headers
                "usr/include/",
                "usr/lib/gcc/x86_64-linux-gnu/13/include/",
                "usr/include/linux/"
        });

        return part;
    }

    private Map<String, Object> createGraalVMInstall(RockProjectSettings settings, BuildRockcraftOptions options) {
        Map<String,Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("stage-snaps", new String[] {"graalvm-jdk/v21/stable"});
        part.put("stage", new String[] {"graalvm-ce/**/*"});
        return part;
    }

    private Map<String, Object> createBuildTool(RockProjectSettings settings, BuildRockcraftOptions options) {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        if (settings.getBuildSystem() == BuildSystem.maven) {
            part.put("stage-packages", new String[]{"maven"});
            part.put("stage", new String[]{
                    "etc/maven",
                    "usr/share/maven",
                    "usr/share/java",
            });
        } else if (settings.getBuildSystem() == BuildSystem.gradle) {
            part.put("build-packages", new String[]{"unzip", "wget"});
            String sb = String.format("wget https://services.gradle.org/distributions/gradle-%s-bin.zip\n", settings.getBuildSystemVersion()) +
                    String.format("unzip -o -qq gradle-%s-bin.zip\n", settings.getBuildSystemVersion()) +
                    "mkdir -p $CRAFT_PART_INSTALL/usr/share/gradle\n" +
                    "mkdir -p $CRAFT_PART_INSTALL/usr/bin\n" +
                    "rm -rf $CRAFT_PART_INSTALL/usr/share/gradle/*\n" +
                    String.format("mv gradle-%s/* $CRAFT_PART_INSTALL/usr/share/gradle/\n", settings.getBuildSystemVersion()) +
                    "cd $CRAFT_PART_INSTALL/ && ln -s --relative usr/share/gradle/bin/gradle usr/bin/\n" +
                    "craftctl default\n";
            part.put("override-build", sb);
        }
        return part;
    }

    private Map<String, Object> createMavenRepository(RockProjectSettings settings, BuildRockcraftOptions options, List<File> files) {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        String source = settings.getRockOutput().relativize(files.get(0).toPath()).toString();
        part.put("source", source);
        part.put("source-type", "local");
        part.put("after", new String[]{"dependencies"});
        String commands = "mkdir -p ${CRAFT_PART_INSTALL}/home/ubuntu/.m2/repository/\n" +
                "cp -r * ${CRAFT_PART_INSTALL}/home/ubuntu/.m2/repository/\n" +
                "chown -R 1000:1000 ${CRAFT_PART_INSTALL}/home/ubuntu\n" +
                "craftctl default\n";
        part.put("override-build", commands);
        return part;
    }

    private Map<String, Object> createDependenciesPart() {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        List<String> slices = getOptions().getSlices();
        // slices mandated for development rock
        slices.addAll(Arrays.asList(
                "apt_apt-get",
                "base-files_base",
                "base-files_chisel",
                "base-files_release-info",
                "base-passwd_data",
                "bash_bins",
                "ca-certificates_data",
                "coreutils_bins",
                "debianutils_which",
                "findutils_bins",
                "grep_bins",
                "gzip_bins",
                "tar_bins",
                "wget_bins",
                "zstd_bins",
                "debianutils_run-parts",
                "libc-bin_getent"));
        slices.add("git_bins");
        slices.add("git_http-support");

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
        overrideCommands.append("# ignore if group is already created\n");
        overrideCommands.append("groupadd -f -R ${CRAFT_PART_INSTALL} -g 1000 ubuntu || [[ $? -eq 4 || $? -eq 9 ]]\n");
        overrideCommands.append("# ignore if user is already created\n");
        overrideCommands.append("useradd -s /usr/bin/sh --home-dir /home/ubuntu --create-home -R ${CRAFT_PART_INSTALL} -g ubuntu -u 1000 ubuntu || [[ $? -eq 4 || $? -eq 9 ]]\n");
        overrideCommands.append("chown -R 1000:1000 ${CRAFT_PART_INSTALL}/home/ubuntu");
        overrideCommands.append("\ncraftctl default\n");
        part.put("override-build", overrideCommands.toString());
        return part;
    }

    private Map<String, Object> createOpenJDKPart() {
        Map<String, Object> part = new HashMap<>();
        part.put("plugin", "nil");
        part.put("source", ".");
        part.put("build-packages", new String[]{options.getBuildPackage()});
        part.put("after", new String[]{"dependencies"});
        List<String> slices = new ArrayList<>();
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
        overrideCommands.append("cd ${CRAFT_PART_INSTALL} && mkdir -p usr/bin && PATH=/usr/bin find . -type f -name java -exec ln -sf --relative {} ${CRAFT_PART_INSTALL}/usr/bin/ \\;\n");
        overrideCommands.append("mkdir -p ${CRAFT_PART_INSTALL}/etc/ssl/certs/java/ &&  cp /etc/ssl/certs/java/cacerts ${CRAFT_PART_INSTALL}/etc/ssl/certs/java/cacerts\n");
        overrideCommands.append("\ncraftctl default\n");
        part.put("override-build", overrideCommands.toString());
        return part;
    }
}
