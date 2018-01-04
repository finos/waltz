/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
        Assert.assertFalse(entityRef.description().isPresent());
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

        Assert.assertTrue(entityRef.description().isPresent());
        Assert.assertTrue(entityRef.description().get() == "Kangaroo is an application");
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