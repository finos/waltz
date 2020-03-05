package com.khartec.waltz.model.rating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.rating.AuthoritativenessRating
 *
 * @author Diffblue JCover
 */

public class AuthoritativenessRatingTest {

    @Test
    public void valuesReturnsPRIMARYSECONDARYNO_OPINIONDISCOURAGED() {
        AuthoritativenessRating[] result = AuthoritativenessRating.values();
        assertThat(result[0], is(AuthoritativenessRating.PRIMARY));
        assertThat(result[1], is(AuthoritativenessRating.SECONDARY));
        assertThat(result[2], is(AuthoritativenessRating.NO_OPINION));
        assertThat(result[3], is(AuthoritativenessRating.DISCOURAGED));
    }
}
