/**
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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Creates Yaml instance with pre-configured output style
 */
public abstract class YamlFactory {

    /**
     * Tag class for the empty value
     */
    public static class Empty {
    }

    /**
     * Representer class to provide special formatting
     */
    private static class NullRepresenter extends Representer {
        public NullRepresenter(DumperOptions options) {
            super(options);
            this.multiRepresenters.put(Empty.class, new RepresentMap() {
                @Override
                public org.yaml.snakeyaml.nodes.Node representData(Object data) {
                        return representScalar(Tag.NULL, ""); // Represent as a scalar with no value
                }
            });
        }
    }

    /**
     * Creates a pre-configured Yaml instance
     * @return yaml
     */
    public static Yaml createYaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        NullRepresenter representer = new NullRepresenter(dumperOptions);
        return new Yaml(representer, dumperOptions);
    }
}
