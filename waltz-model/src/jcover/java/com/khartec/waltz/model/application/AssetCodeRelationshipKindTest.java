package com.khartec.waltz.model.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.application.AssetCodeRelationshipKind
 *
 * @author Diffblue JCover
 */

public class AssetCodeRelationshipKindTest {

    @Test
    public void valuesReturnsPARENTCHILDSIBLINGSHARINGNONE() {
        AssetCodeRelationshipKind[] result = AssetCodeRelationshipKind.values();
        assertThat(result[0], is(AssetCodeRelationshipKind.PARENT));
        assertThat(result[1], is(AssetCodeRelationshipKind.CHILD));
        assertThat(result[2], is(AssetCodeRelationshipKind.SIBLING));
        assertThat(result[3], is(AssetCodeRelationshipKind.SHARING));
        assertThat(result[4], is(AssetCodeRelationshipKind.NONE));
    }
}
