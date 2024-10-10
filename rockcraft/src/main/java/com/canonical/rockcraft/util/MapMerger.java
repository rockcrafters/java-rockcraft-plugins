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
package com.canonical.rockcraft.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility class to merge two maps.
 * It is used to combine Yaml files
 */
public class MapMerger {
    /**
     * Recursively merges two map of maps
     *
     * @param m1 - first map of maps
     * @param m2 - second map of maps
     * @return union of m1 and m2. m2 values overwrite m1
     */
    public static Map<String, Object> merge(Map<String, Object> m1, Map<String, Object> m2) {
        Map<String, Object> ret = new HashMap<String, Object>(m1);
        if (m2 == null) {
            return ret;
        }
        for (String key : ret.keySet()) {
            Object m2Value = m2.get(key);
            if (m2Value == null)
                continue;
            ret.merge(key, m2Value, new BiFunction<Object, Object, Object>() {
                public Object apply(Object oldValue, Object newValue) {
                    if (oldValue instanceof Map && newValue instanceof Map) {
                        return MapMerger.merge((Map<String, Object>) oldValue, (Map<String, Object>) newValue);
                    }
                    return newValue;
                }
            });
        }
        for (String key : m2.keySet()) {
            if (!ret.containsKey(key)) {
                ret.put(key, m2.get(key));
            }
        }
        return ret;
    }
}
