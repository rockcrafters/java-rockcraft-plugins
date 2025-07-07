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

public class BuildRockcraftOptions extends CommonRockcraftOptions {
    private String[] buildGoals = new String[0];
    private boolean withGradleCache = false;


    public BuildRockcraftOptions() {
        super();
        setBuildPackage("openjdk-21-jdk-headless");
    }

    public String[] getBuildGoals() {
        return buildGoals;
    }

    public void setBuildGoals(String[] goal) {
        this.buildGoals = goal;
    }

    public boolean isWithGradleCache() {
        return withGradleCache;
    }

    public void setWithGradleCache(boolean withGradleCache) {
        this.withGradleCache = withGradleCache;
    }
}
