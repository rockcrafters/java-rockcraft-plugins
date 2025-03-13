/*
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
package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.RockcraftOptions;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

/**
 * Base class for rockcraft tasks
 */
public class AbstractRockcraftTask extends DefaultTask {

    @Input
    private final RockcraftOptions options;

    /**
     * Constructs the CreateRockcraft task
     *
     * @param options - plugin options
     */
    @Inject
    public AbstractRockcraftTask(RockcraftOptions options) {
        this.options = options;
    }

    /**
     * Gets rockcraft project options
     *
     * @return rockcraft project options
     */
    protected RockcraftOptions getOptions() {
        return options;
    }
}
