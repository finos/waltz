/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.model.immediate_hierarchy;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.ParentIdProvider;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    public void selfMustBePresent() {
        assertThrows(IllegalArgumentException.class,
                () -> ImmediateHierarchyUtilities.build(-1, all));
    }

    @Test
    public void mustHaveSomeUnits() {
        assertThrows(IllegalArgumentException.class,
                () -> ImmediateHierarchyUtilities.build(1, Collections.emptyList()));
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