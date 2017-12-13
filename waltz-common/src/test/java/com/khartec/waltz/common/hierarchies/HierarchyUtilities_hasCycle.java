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

package com.khartec.waltz.common.hierarchies;

import com.khartec.waltz.common.hierarchy.Forest;
import com.khartec.waltz.common.hierarchy.HierarchyUtilities;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class HierarchyUtilities_hasCycle {

    @Test
    public void noCycles() {
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.TWO_TREES);
        assertFalse(HierarchyUtilities.hasCycle(forest));
    }

    @Test
    public void withCycles() {
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.CIRCULAR);
        assertTrue(HierarchyUtilities.hasCycle(forest));
    }

}
