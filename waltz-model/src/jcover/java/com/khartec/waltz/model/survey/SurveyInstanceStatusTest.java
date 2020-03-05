package com.khartec.waltz.model.survey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.survey.SurveyInstanceStatus
 *
 * @author Diffblue JCover
 */

public class SurveyInstanceStatusTest {

    @Test
    public void valuesReturnsNOT_STARTEDIN_PROGRESSCOMPLETEDREJECTEDWITHDRAWNAPPROVED() {
        SurveyInstanceStatus[] result = SurveyInstanceStatus.values();
        assertThat(result[0], is(SurveyInstanceStatus.NOT_STARTED));
        assertThat(result[1], is(SurveyInstanceStatus.IN_PROGRESS));
        assertThat(result[2], is(SurveyInstanceStatus.COMPLETED));
        assertThat(result[3], is(SurveyInstanceStatus.REJECTED));
        assertThat(result[4], is(SurveyInstanceStatus.WITHDRAWN));
        assertThat(result[5], is(SurveyInstanceStatus.APPROVED));
    }
}
