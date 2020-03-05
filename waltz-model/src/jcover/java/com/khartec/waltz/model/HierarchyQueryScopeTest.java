package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.HierarchyQueryScope
 *
 * @author Diffblue JCover
 */

public class HierarchyQueryScopeTest {

    @Test
    public void determineDownwardsScopeForKindKindIsACTORReturnsEXACT() {
        assertThat(HierarchyQueryScope.determineDownwardsScopeForKind(EntityKind.ACTOR), is(HierarchyQueryScope.EXACT));
    }

    @Test
    public void determineUpwardsScopeForKindKindIsACTORReturnsEXACT() {
        assertThat(HierarchyQueryScope.determineUpwardsScopeForKind(EntityKind.ACTOR), is(HierarchyQueryScope.EXACT));
    }

    @Test
    public void valuesReturnsEXACTPARENTSCHILDREN() {
        HierarchyQueryScope[] result = HierarchyQueryScope.values();
        assertThat(result[0], is(HierarchyQueryScope.EXACT));
        assertThat(result[1], is(HierarchyQueryScope.PARENTS));
        assertThat(result[2], is(HierarchyQueryScope.CHILDREN));
    }
}
