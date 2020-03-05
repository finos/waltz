package com.khartec.waltz.model.entity_relationship;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.entity_relationship.Directionality
 *
 * @author Diffblue JCover
 */

public class DirectionalityTest {

    @Test
    public void valuesReturnsANYSOURCETARGET() {
        Directionality[] result = Directionality.values();
        assertThat(result[0], is(Directionality.ANY));
        assertThat(result[1], is(Directionality.SOURCE));
        assertThat(result[2], is(Directionality.TARGET));
    }
}
