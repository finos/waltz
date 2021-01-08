package com.khartec.waltz.common.hierarchy;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HierarchyUtilities_assignDepths {

    @Test
    public void simpleAssignDepths(){
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.TWO_TREES);
        Map m = HierarchyUtilities.assignDepths(forest);
        assertTrue(m.containsKey("a"));
        assertEquals(1, m.get("a"));
    }

    @Test
    public void simpleAssignDepthsWithEmptyTree(){
        Forest<Void, String> forest = HierarchyUtilities.toForest(SampleData.EMPTY_TREE);
        Map m = HierarchyUtilities.assignDepths(forest);
        assertEquals(0, m.size());
    }
}
