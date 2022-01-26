package org.finos.waltz.common.hierarchy;

import org.junit.jupiter.api.Test;

import java.util.List;

public class HierarchyUtilities_parentsTest {

    @Test
    public void simpleParents(){
        Forest forest = HierarchyUtilities.toForest(SampleData.TWO_TREES);
        List result = HierarchyUtilities.parents((Node)forest.getAllNodes().get("b"));
        System.out.println(result.get(0));
    }

}
