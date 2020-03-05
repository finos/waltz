package com.khartec.waltz.model.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.application.ApplicationKind
 *
 * @author Diffblue JCover
 */

public class ApplicationKindTest {

    @Test
    public void valuesReturnsIN_HOUSEINTERNALLY_HOSTEDEXTERNALLY_HOSTEDEUCTHIRD_PARTYCUSTOMISEDEXTERNAL() {
        ApplicationKind[] result = ApplicationKind.values();
        assertThat(result[0], is(ApplicationKind.IN_HOUSE));
        assertThat(result[1], is(ApplicationKind.INTERNALLY_HOSTED));
        assertThat(result[2], is(ApplicationKind.EXTERNALLY_HOSTED));
        assertThat(result[3], is(ApplicationKind.EUC));
        assertThat(result[4], is(ApplicationKind.THIRD_PARTY));
        assertThat(result[5], is(ApplicationKind.CUSTOMISED));
        assertThat(result[6], is(ApplicationKind.EXTERNAL));
    }
}
