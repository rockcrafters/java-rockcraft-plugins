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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RockNameUtilTest {

    @Test
    public void testFormatRockName() {
        assertEquals("test", RockNameUtil.formatRockName("test"));
        assertEquals("test", RockNameUtil.formatRockName("TesT"));
        assertEquals("test", RockNameUtil.formatRockName(" TesT "));
        assertEquals("te-st", RockNameUtil.formatRockName("Te  sT"));
        assertEquals("te-st", RockNameUtil.formatRockName("Te-  sT"));
        assertEquals("te-st", RockNameUtil.formatRockName("Te-  -sT"));
        assertEquals("te-st", RockNameUtil.formatRockName("-Te-  -sT"));
        assertEquals("te-st", RockNameUtil.formatRockName("-Te-  -sT-"));
        assertEquals("rock", RockNameUtil.formatRockName("-Te- ! -sT"));
    }

    @Test
    public void testRockNameLength() {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            name.append("0123456789");
        }
        assertEquals(40, RockNameUtil.formatRockName(name.toString()).length());
    }
}
