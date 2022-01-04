package org.finos.waltz.service.survey;

import org.finos.waltz.data.survey.SurveyInstanceDao;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.data.survey.SurveyQuestionResponseDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.survey.inclusion_evaluator.QuestionPredicateEvaluator;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.service.survey.SurveyInstanceUtilities.getVal;

@Service
public class SurveyInstanceEvaluator {

    private final DSLContext dsl;
    private final SurveyQuestionDao questionDao;
    private final SurveyInstanceDao instanceDao;
    private final SurveyQuestionResponseDao responseDao;


    @Autowired
    public SurveyInstanceEvaluator(DSLContext dsl,
                                   SurveyQuestionDao questionDao,
                                   SurveyInstanceDao instanceDao,
                                   SurveyQuestionResponseDao responseDao) {
        this.dsl = dsl;
        this.questionDao = questionDao;
        this.instanceDao = instanceDao;
        this.responseDao = responseDao;
    }


    public ImmutableSurveyInstanceFormDetails eval(long surveyInstanceId) {
        List<SurveyQuestion> qs = loadQuestions(surveyInstanceId);
        Map<Long, SurveyQuestionResponse> responsesByQuestionId = loadResponses(surveyInstanceId);

        SurveyInstance instance = instanceDao.getById(surveyInstanceId);
        EntityReference subjectRef = instance.surveyEntity();

        List<SurveyQuestion> activeQs = QuestionPredicateEvaluator.eval(dsl, qs, subjectRef, responsesByQuestionId);
        Set<Long> missingMandatoryQuestions = determineMissingMandatoryQuestions(activeQs, responsesByQuestionId);

        return ImmutableSurveyInstanceFormDetails.builder()
                .activeQuestions(activeQs)
                .missingMandatoryQuestionIds(missingMandatoryQuestions)
                .build();
    }


    private static Set<Long> determineMissingMandatoryQuestions(List<SurveyQuestion> activeQs,
                                                                Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        return activeQs
                .stream()
                .filter(SurveyQuestion::isMandatory)
                .filter(q -> {
                    SurveyQuestionResponse resp = responsesByQuestionId.get(q.id().get());
                    Optional<?> val = getVal(q, resp);
                    return ! val.isPresent();
                })
                .map(q -> q.id().get())
                .collect(Collectors.toSet());
    }


    private Map<Long, SurveyQuestionResponse> loadResponses(long surveyInstanceId) {
        return indexBy(
                responseDao.findForInstance(surveyInstanceId),
                r -> r.questionResponse().questionId(),
                SurveyInstanceQuestionResponse::questionResponse);
    }


    private List<SurveyQuestion> loadQuestions(long surveyInstanceId) {
        return questionDao.findForSurveyInstance(surveyInstanceId);
    }


}
