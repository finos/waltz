package org.finos.waltz.service.survey;

import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionResponse;

import java.util.Optional;

public class SurveyInstanceUtilities {

    public static Optional<? extends Object> getVal(SurveyQuestion question,
                                                    SurveyQuestionResponse resp) {
        if (resp == null) {
            return Optional.empty();
        }

        switch (question.fieldType()) {
            case TEXT:
            case TEXTAREA:
            case DROPDOWN:
                return resp.stringResponse();
            case NUMBER:
                return resp.numberResponse();
            case DATE:
                return resp.dateResponse();
            case BOOLEAN:
                return resp.booleanResponse();
            case DROPDOWN_MULTI_SELECT:
            case STRING_LIST:
                return resp.listResponse();
            case APPLICATION:
            case PERSON:
                return resp.entityResponse();
            case MEASURABLE_MULTI_SELECT:
            case LEGAL_ENTITY:
                return resp.entityListResponse();
            case ARC:
                return resp.jsonResponse();
            default:
                return Optional.empty();
        }
    }
}
