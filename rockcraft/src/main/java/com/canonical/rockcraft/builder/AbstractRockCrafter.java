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

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRockCrafter {
    public static final String PARTS = "parts";
    protected final RockProjectSettings settings;
    protected final CommonRockcraftOptions options;
    protected final List<File> artifacts;

    public AbstractRockCrafter(RockProjectSettings settings, CommonRockcraftOptions options, List<File> artifacts) {
        this.settings = settings;
        this.options = options;
        this.artifacts = artifacts;
    }

    protected List<File> getArtifacts() { return artifacts; }

    protected RockProjectSettings getSettings() { return settings; }

    protected Map<String, Object> getPlatforms() {
        HashMap<String, Object> arches = new HashMap<>();
        for (RockArchitecture a : getOptions().getArchitectures())
            arches.put(String.valueOf(a), new YamlFactory.Empty());
        if (arches.isEmpty())
            arches.put("amd64", new YamlFactory.Empty());
        return arches;
    }

    protected CommonRockcraftOptions getOptions() {
        return options;
    }

    /**
     * Writes a rockcraft.yaml file to the output directory
     *
     * @throws IOException - the method fails to write rockcraft.yaml
     */
    public void writeRockcraft() throws IOException {
        getSettings().getRockOutput().toFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getSettings().getRockOutput().resolve(IRockcraftNames.ROCKCRAFT_YAML).toFile()))) {
            String rockcraft = createRockcraft(getSettings().getRockOutput(), getArtifacts());
            writer.write(rockcraft);
        }
    }

    protected abstract String createRockcraft(Path root, List<File> files) throws IOException;

    protected Map<String, Object> createCommonSection() throws IOException {
        Map<String, Object> rockcraft = new HashMap<>();
        rockcraft.put(IRockcraftNames.ROCKCRAFT_NAME, RockNameUtil.formatRockName(getSettings().getName()));
        rockcraft.put(IRockcraftNames.ROCKCRAFT_VERSION, String.valueOf(getSettings().getVersion()));
        rockcraft.put("summary", getOptions().getSummary());
        rockcraft.put( "run-user", "_daemon_");
        Path description = getOptions().getDescription();
        if (description != null) {
            File descriptionFile = getSettings().getProjectPath().resolve(description).toFile();
            if (!descriptionFile.exists())
                throw new UnsupportedOperationException("Rockcraft plugin description file does not exist.");
            rockcraft.put("description", new String(Files.readAllBytes(descriptionFile.toPath())));
        } else {
            rockcraft.put("description", "");
        }

        rockcraft.put("platforms", getPlatforms());
        rockcraft.put("base", "bare");
        rockcraft.put("build-base", "ubuntu@24.04");
        return rockcraft;
    }

    protected Map<String, Object> loadRockcraftSnippet(Yaml yaml) throws IOException {
        Map<String, Object> rockcraftYaml = new HashMap<>();
        if (getOptions().getRockcraftYaml() != null) {
            File rockcraftFile = getSettings().getProjectPath().resolve(getOptions().getRockcraftYaml()).toFile();
            if (!rockcraftFile.exists()) {
                throw new UnsupportedOperationException("Rockcraft file " + rockcraftFile + " does not exist.\n");
            }
            try (FileInputStream is = new FileInputStream(rockcraftFile)) {
                rockcraftYaml = yaml.load(is);
                if (rockcraftYaml == null) {
                    throw new UnsupportedOperationException("Rockcraft file "+ rockcraftFile + " can not be parsed.\n");
                }
            }
        }
        return rockcraftYaml;
    }
}
