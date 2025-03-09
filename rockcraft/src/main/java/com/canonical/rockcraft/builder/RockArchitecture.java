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

/**
 * The list of supported Ubuntu architectures
 */
public enum RockArchitecture {
    /**
     * AMD64
     */
    amd64,
    /**
     * ARM64
     */
    arm64,
    /**
     * ARM hard float
     */
    armhf,
    /**
     * I386
     */
    i386,
    /**
     * PowerPC 64 EL
     */
    ppc64el,
    /**
     * RISCV
     */
    riscv64,
    /**
     * S390X
     */
    s390x
}
