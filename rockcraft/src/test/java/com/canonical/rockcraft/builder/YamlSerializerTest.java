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

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlSerializerTest {
    @Test
    public void testEmptyValue() {
        Yaml yaml = YamlFactory.createYaml();
        HashMap<Object, Object> test = new HashMap<>();
        test.put("key", new YamlFactory.Empty());
        assertEquals("key:\n", yaml.dump(test));
    }
}
