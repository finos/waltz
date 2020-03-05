package com.khartec.waltz.model.staticpanel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.staticpanel.ContentKind
 *
 * @author Diffblue JCover
 */

public class ContentKindTest {

    @Test
    public void valuesReturnsHTMLMARKDOWN() {
        ContentKind[] result = ContentKind.values();
        assertThat(result[0], is(ContentKind.HTML));
        assertThat(result[1], is(ContentKind.MARKDOWN));
    }
}
