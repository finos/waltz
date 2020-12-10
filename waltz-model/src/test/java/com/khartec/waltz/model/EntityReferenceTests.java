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

package com.khartec.waltz.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EntityReferenceTests {

    @Test(expected = IllegalArgumentException.class)
    public void ThrowsWhenMapIsNull() {
        EntityReference.mkRef(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ThrowsWhenMapMissingKind() {
        Map map = new HashMap();
        map.put("id", 5);
        EntityReference.mkRef(map);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ThrowsWhenMapMissingId() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        EntityReference.mkRef(map);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ThrowsWhenIdNull() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", null);

        EntityReference.mkRef(map);
    }


    @Test()
    public void BuildEntityRefWhenMapMissingNameAndDesc() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);

        EntityReference entityRef = EntityReference.mkRef(map);
        Assert.assertTrue(entityRef.id() == 5);
        Assert.assertTrue(entityRef.kind() == EntityKind.APPLICATION);
        Assert.assertFalse(entityRef.name().isPresent());
        Assert.assertNull(entityRef.description());
    }


    @Test()
    public void BuildEntityRefWithAllValuesPresentCorrectly() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);
        map.put("name", "Kangaroo");
        map.put("description", "Kangaroo is an application");

        EntityReference entityRef = EntityReference.mkRef(map);
        Assert.assertTrue(entityRef.id() == 5);
        Assert.assertTrue(entityRef.kind() == EntityKind.APPLICATION);

        Assert.assertTrue(entityRef.name().isPresent());
        Assert.assertTrue(entityRef.name().get() == "Kangaroo");

        Assert.assertNotNull(entityRef.description());
        Assert.assertTrue(entityRef.description() == "Kangaroo is an application");
    }


    @Test()
    public void BuildEntityRefWithNullForNameCorrectly() {
        Map map = new HashMap();
        map.put("kind", "APPLICATION");
        map.put("id", 5);
        map.put("name", null);

        EntityReference entityRef = EntityReference.mkRef(map);
        Assert.assertTrue(entityRef.id() == 5);
        Assert.assertTrue(entityRef.kind() == EntityKind.APPLICATION);

        Assert.assertFalse(entityRef.name().isPresent());
    }
}