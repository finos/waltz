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

package org.finos.waltz.model;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EntityReferenceTests {

    @Test
    public void ThrowsWhenMapIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> EntityReference.mkRef(null));
    }


    @Test
    public void ThrowsWhenMapMissingKind() {
        Map map = new HashMap();
        map.put("id", 5);
        assertThrows(IllegalArgumentException.class,
                () -> EntityReference.mkRef(map));
    }


    @Test
    public void ThrowsWhenMapMissingId() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        assertThrows(IllegalArgumentException.class,
                () -> EntityReference.mkRef(map));
    }


    @Test
    public void ThrowsWhenIdNull() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", null);
        assertThrows(IllegalArgumentException.class,
                () -> EntityReference.mkRef(map));
    }


    @Test()
    public void BuildEntityRefWhenMapMissingNameAndDesc() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);

        EntityReference entityRef = EntityReference.mkRef(map);
        assertTrue(entityRef.id() == 5);
        assertTrue(entityRef.kind() == EntityKind.APPLICATION);
        assertFalse(entityRef.name().isPresent());
        assertNull(entityRef.description());
    }


    @Test()
    public void BuildEntityRefWithAllValuesPresentCorrectly() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);
        map.put("name", "Kangaroo");
        map.put("description", "Kangaroo is an application");

        EntityReference entityRef = EntityReference.mkRef(map);
        assertEquals(5, entityRef.id());
        assertSame(entityRef.kind(), EntityKind.APPLICATION);

        assertTrue(entityRef.name().isPresent());
        assertSame("Kangaroo", entityRef.name().get());

        assertNotNull(entityRef.description());
        assertSame("Kangaroo is an application", entityRef.description());
    }


    @Test()
    public void BuildEntityRefWithNullForNameCorrectly() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);
        map.put("name", null);

        EntityReference entityRef = EntityReference.mkRef(map);
        assertTrue(entityRef.id() == 5);
        assertTrue(entityRef.kind() == EntityKind.APPLICATION);

        assertFalse(entityRef.name().isPresent());
    }
}