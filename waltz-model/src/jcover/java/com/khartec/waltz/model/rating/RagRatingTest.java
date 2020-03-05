package com.khartec.waltz.model.rating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.rating.RagRating
 *
 * @author Diffblue JCover
 */

public class RagRatingTest {

    @Test
    public void valuesReturnsRAGZX() {
        RagRating[] result = RagRating.values();
        assertThat(result[0], is(RagRating.R));
        assertThat(result[1], is(RagRating.A));
        assertThat(result[2], is(RagRating.G));
        assertThat(result[3], is(RagRating.Z));
        assertThat(result[4], is(RagRating.X));
    }
}
