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
