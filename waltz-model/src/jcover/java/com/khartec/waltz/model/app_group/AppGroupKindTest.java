package com.khartec.waltz.model.app_group;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.app_group.AppGroupKind
 *
 * @author Diffblue JCover
 */

public class AppGroupKindTest {

    @Test
    public void valuesReturnsPUBLICPRIVATE() {
        AppGroupKind[] result = AppGroupKind.values();
        assertThat(result[0], is(AppGroupKind.PUBLIC));
        assertThat(result[1], is(AppGroupKind.PRIVATE));
    }
}
