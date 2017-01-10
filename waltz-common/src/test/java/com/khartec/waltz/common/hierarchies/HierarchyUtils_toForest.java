/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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


    private Optional<Node<Void, String>> find(Collection<Node<Void, String>> trees, String id) {
        return trees.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst();
    }


}
