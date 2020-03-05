package com.khartec.waltz.model.rating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.rating.RatingScheme
 *
 * @author Diffblue JCover
 */

public class RatingSchemeTest {

    @Test
    public void toList() {
        assertThat(RatingScheme.toList().size(), is(6));
    }
}
