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
package com.canonical.rockcraft.gradle;

import org.gradle.api.artifacts.component.ComponentIdentifier;

import java.util.Set;

/**
 * Dependency lookup result for maven pom
 * @param dependencies - dependencies
 * @param dependencyManagement - BOMs
 */
public record DependencyResolutionResult(Set<ComponentIdentifier> dependencies, Set<ComponentIdentifier> dependencyManagement) {
}
