package com.khartec.waltz.model.survey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.survey.SurveyQuestionFieldType
 *
 * @author Diffblue JCover
 */

public class SurveyQuestionFieldTypeTest {

    @Test
    public void valuesReturnsAPPLICATIONBOOLEANDATEDROPDOWNDROPDOWN_MULTI_SELECTNUMBERPERSONTEXTTEXTAREA() {
        SurveyQuestionFieldType[] result = SurveyQuestionFieldType.values();
        assertThat(result[0], is(SurveyQuestionFieldType.APPLICATION));
        assertThat(result[1], is(SurveyQuestionFieldType.BOOLEAN));
        assertThat(result[2], is(SurveyQuestionFieldType.DATE));
        assertThat(result[3], is(SurveyQuestionFieldType.DROPDOWN));
        assertThat(result[4], is(SurveyQuestionFieldType.DROPDOWN_MULTI_SELECT));
        assertThat(result[5], is(SurveyQuestionFieldType.NUMBER));
        assertThat(result[6], is(SurveyQuestionFieldType.PERSON));
        assertThat(result[7], is(SurveyQuestionFieldType.TEXT));
        assertThat(result[8], is(SurveyQuestionFieldType.TEXTAREA));
    }
}
