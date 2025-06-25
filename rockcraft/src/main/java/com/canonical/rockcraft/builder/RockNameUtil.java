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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rockcraft rock name can only use ASCII lowercase letters, numbers, and hyphens.
 * They must start with a lowercase letter, may not end with a hyphen, and may not
 * have two hyphens in a row.
 */
public class RockNameUtil {
    /**
     * Formats name according to rockcraft rules
     * @param name - project name
     * @return formatted rock name
     */
    public static String formatRockName(String name) {
        if (name == null) {
            throw new RuntimeException("Rock name can not be null");
        }
        String rockName = name.toLowerCase()
                .replace('-', ' ') // replace '-' with ' ' to string leading and trailing hyphens
                .trim() // trim to remove leading or trailing spaces
                .replace(' ', '-') // replace any spaces with hyphens
                .replaceAll("-+", "-"); // replace duplicate hyphens with a single one
        if (!Pattern.compile("^[a-z0-9-]+$").matcher(rockName).matches()) {
            throw new RuntimeException("Unable to create name for the rock from "+ name + ". Please use custom rockcraft.yaml");
        }
        return rockName;
    }
}
