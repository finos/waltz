package com.khartec.waltz.model.survey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.survey.SurveyIssuanceKind
 *
 * @author Diffblue JCover
 */

public class SurveyIssuanceKindTest {

    @Test
    public void valuesReturnsGROUPINDIVIDUAL() {
        SurveyIssuanceKind[] result = SurveyIssuanceKind.values();
        assertThat(result[0], is(SurveyIssuanceKind.GROUP));
        assertThat(result[1], is(SurveyIssuanceKind.INDIVIDUAL));
    }
}
