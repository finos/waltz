package com.khartec.waltz.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.IdSelectionOptions
 *
 * @author Diffblue JCover
 */

public class IdSelectionOptionsTest {

    @Test
    public void determineDefaultScopeKindIsACTORReturnsEXACT() {
        assertThat(IdSelectionOptions.determineDefaultScope(EntityKind.ACTOR), is(HierarchyQueryScope.EXACT));
    }
}
