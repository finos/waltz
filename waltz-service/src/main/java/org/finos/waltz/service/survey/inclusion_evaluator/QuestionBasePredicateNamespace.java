package org.finos.waltz.service.survey.inclusion_evaluator;

import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.finos.waltz.service.survey.SurveyInstanceUtilities;

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
                .flatMap(resp -> SurveyInstanceUtilities.getVal(referencedQuestion, resp)))
                .orElse(defaultValue);
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
