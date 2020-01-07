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

package com.khartec.waltz.common.hierarchies;

import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import com.khartec.waltz.common.hierarchy.Node;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class HierarchyUtils_toForest {



    @Test
    public void circular() {
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.CIRCULAR);
        assertEquals(3, forest.getAllNodes().size());
        assertEquals(0, forest.getRootNodes().size());
    }


    @Test
    public void twoTrees() {
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.TWO_TREES);

        assertEquals(2, forest.getRootNodes().size());

        Optional<Node<Void, String>> maybeA = find(forest.getRootNodes(), "a");
        Optional<Node<Void, String>> maybeF = find(forest.getRootNodes(), "f");

        assertTrue(maybeA.isPresent());
        assertEquals(2, maybeA.get().getChildren().size());

        assertTrue(maybeF.isPresent());
        assertEquals(1, maybeF.get().getChildren().size());
    }

    @Test
    public void selfRef() {
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.SELF_REFERENCE);
        System.out.println(forest);
        System.out.println(forest.getRootNodes());
        System.out.println(HierarchyUtilities.assignDepths(forest));
        forest.getAllNodes().entrySet().forEach(e -> System.out.println(HierarchyUtilities.parents(e.getValue())));
    }


    private Optional<Node<Void, String>> find(Collection<Node<Void, String>> trees, String id) {
        return trees.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst();
    }


}
