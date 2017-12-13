/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.immediate_hierarchy;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImmediateHierarchyUtilitiesTest {

    private static class Dummy implements IdProvider, ParentIdProvider {
        private Long id;
        private Long parentId;

        public Dummy(Long id, Long parentId) {
            this.id = id;
            this.parentId = parentId;
        }

        @Override
        public Optional<Long> id() {
            return Optional.ofNullable(id);
        }

        @Override
        public Optional<Long> parentId() {
            return Optional.ofNullable(parentId);
        }

        @Override
        public String toString() {
            return "id: "+id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dummy dummy = (Dummy) o;

            if (id != null ? !id.equals(dummy.id) : dummy.id != null) return false;
            return parentId != null ? parentId.equals(dummy.parentId) : dummy.parentId == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
            return result;
        }
    }


    Dummy d1 = new Dummy(1L, null);

    Dummy d11 = new Dummy(11L, d1.id);
    Dummy d12 = new Dummy(12L, d1.id);

    Dummy d111 = new Dummy(111L, d11.id);
    Dummy d112 = new Dummy(112L, d11.id);

    ArrayList<Dummy> all = ListUtilities.newArrayList(d1, d11, d12, d111, d112);


    @Test
    public void build() throws Exception {


        System.out.println(ImmediateHierarchyUtilities.build(d1.id, all));
        System.out.println(ImmediateHierarchyUtilities.build(d11.id, all));
        System.out.println(ImmediateHierarchyUtilities.build(d12.id, all));
        System.out.println(ImmediateHierarchyUtilities.build(d112.id, all));

        ImmediateHierarchy<Dummy> r1 = ImmediateHierarchyUtilities.build(d1.id, all);
        ImmediateHierarchy<Dummy> r11 = ImmediateHierarchyUtilities.build(d11.id, all);
        ImmediateHierarchy<Dummy> r12 = ImmediateHierarchyUtilities.build(d12.id, all);
        ImmediateHierarchy<Dummy> r112 = ImmediateHierarchyUtilities.build(d112.id, all);

        assertEquals(d1, r1.self());
        assertEquals(empty(), r1.parent());
        assertSize(2, r1.children());
        assertContains(r1.children(), d11, d12);

        assertEquals(d11, r11.self());
        assertEquals(Optional.of(d1), r11.parent());
        assertSize(2, r11.children());
        assertContains(r11.children(), d111, d112);

        assertEquals(d12, r12.self());
        assertEquals(Optional.of(d1), r12.parent());
        assertEmpty(r12.children());

        assertEquals(d112, r112.self());
        assertEquals(Optional.of(d11), r112.parent());
        assertEmpty(r112.children());


    }


    @Test(expected = IllegalArgumentException.class)
    public void selfMustBePresent() {
        ImmediateHierarchyUtilities.build(-1, all);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustHaveSomeUnits() {
        ImmediateHierarchyUtilities.build(1, Collections.emptyList());
    }


    private <X> void assertContains(List<X> xs, X... things) {
        for (X t : things) {
            assertTrue(xs.contains(t));
        }
    }

    private <X> void assertSize(int i, List<X> xs) {
        assertEquals(i, xs.size());
    }

    private <X> void assertEmpty(List<X> xs) {
        assertTrue(xs.isEmpty());
    }

}