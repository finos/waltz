package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 *
 * NOTE: methods in this class may show as unused.  This is to be expected as they are referred to via
 * predicates in survey questions
 */
public class QuestionBasePredicateNamespace {

    private final Map<String, SurveyQuestion> questionsByExtId;
    private final Map<Long, SurveyQuestionResponse> responsesByQuestionId;
    private JexlEngine jexl;


    public QuestionBasePredicateNamespace(List<SurveyQuestion> questions,
                                          Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        this.responsesByQuestionId = responsesByQuestionId;
        questionsByExtId = maybeIndexBy(questions, ExternalIdProvider::externalId);
    }



    public boolean isChecked(String qExtId, boolean defaultValue) {
        return questionsByExtId
                .get(qExtId)
                .id()
                .map(responsesByQuestionId::get)
                .flatMap(SurveyQuestionResponse::booleanResponse)
                .orElse(defaultValue);
    }


    public boolean isChecked(String qExtId) {
        return isChecked(qExtId, false);
    }


    public double numberValue(String qExtId, double defaultValue) {
        return questionsByExtId
                .get(qExtId)
                .id()
                .map(responsesByQuestionId::get)
                .flatMap(SurveyQuestionResponse::numberResponse)
                .orElse(defaultValue);
    }


    public double numberValue(String qExtId) {
        return numberValue(qExtId, 0.0);
    }


    public boolean ditto(String qExtId) {
        SurveyQuestion referencedQuestion = questionsByExtId
                .get(qExtId);

        return referencedQuestion
                .inclusionPredicate()
                .map(p -> jexl.createExpression(p).evaluate(new MapContext()))
                .map(r -> Boolean.valueOf(r.toString()))
                .orElse(true);
    }


    public Object val(String qExtId) {
        return val(qExtId, null);
    }


    public <T> T val(String qExtId, T defaultValue) {
        SurveyQuestion referencedQuestion = questionsByExtId
                .get(qExtId);

        return ((Optional<T>) referencedQuestion
                .id()
                .map(responsesByQuestionId::get)
                .flatMap(resp -> getVal(referencedQuestion, resp)))
                .orElse(defaultValue);
    }


    private Optional<? extends Object> getVal(SurveyQuestion referencedQuestion, SurveyQuestionResponse resp) {
        switch (referencedQuestion.fieldType()) {
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
                return resp.listResponse();
            case APPLICATION:
            case PERSON:
                return resp.entityResponse();
            default:
                return Optional.empty();
        }
    }

    /**
     * Need to pass in the evaluator so that 'recursive' functions can be computed (e.g. 'DITTO')
     * @param jexl
     */
    public void usingEvaluator(JexlEngine jexl) {
        this.jexl = jexl;
    }


    private <K, R> Map<K, R> maybeIndexBy(List<R> values,
                                          Function<R, Optional<K>> keyProvider) {
        return values
                .stream()
                .map(v -> tuple(v, keyProvider.apply(v)))
                .filter(t -> t.v2.isPresent())
                .map(t -> t.map2(Optional::get))
                .collect(Collectors.toMap(t -> t.v2, t -> t.v1, (a, b) -> a));
    }

}
