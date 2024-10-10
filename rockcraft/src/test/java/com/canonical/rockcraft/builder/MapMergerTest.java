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
package com.canonical.rockcraft.builder;

import com.canonical.rockcraft.util.MapMerger;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unchecked")
public class MapMergerTest {

    @Test
    public void testNullMerge() {
        // given map1 (a->b) and map2 null
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("a", "b");
        // when maps are merged
        Map<String, Object> ret = MapMerger.merge(map1, null);
        // then the result is (a->b)
        assertEquals(1, ret.keySet().size());
        assertEquals("b", ret.get("a"));
    }

    @Test
    public void testSimpleMerge() {
        // given map1 (a->b) and map2 (b->c)
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("a", "b");
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map1.put("b", "c");
        // when maps are merged
        Map<String, Object> ret = MapMerger.merge(map1, map2);
        // then the result is (a->b, b->c)
        assertEquals(2, ret.keySet().size());
        assertEquals("b", ret.get("a"));
        assertEquals("c", ret.get("b"));
    }

    public void testReplaceMerge() {
        // given map1 (a->c) and map2 (a->d)
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("a", "c");
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map1.put("a", "d");
        // when maps are merged
        Map<String, Object> ret = MapMerger.merge(map1, map2);
        // then the result is (a->d)
        assertEquals(1, ret.keySet().size());
        assertEquals("d", ret.get("a"));
    }

    public void testSubmapMerge() {
        // given map1 (a->(a->a, b->b, f->f) ) and map2 (a->(a->b, b-c, d->e))
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        HashMap<String, Object> inner = new HashMap<String, Object>();
        inner.put("a", "a");
        inner.put("b", "b");
        inner.put("f", "f");
        map1.put("a", inner);
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        inner = new HashMap<String, Object>();
        inner.put("a", "b");
        inner.put("b", "c");
        inner.put("d", "e");
        map1.put("a", inner);
        // when maps are merged
        Map<String, Object> ret = MapMerger.merge(map1, map2);
        // then the result is (a->(a->b, b->c, d->e, f->f) )
        assertEquals(1, ret.keySet().size());
        ret = (Map<String, Object>) ret.get("a");
        assertEquals(4, ret.keySet().size());
        assertEquals("b", ret.get("a"));
        assertEquals("b", ret.get("a"));
        assertEquals("c", ret.get("b"));
        assertEquals("e", ret.get("d"));
        assertEquals("f", ret.get("f"));
    }
}
