package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.data.survey.SurveyInstanceDao;
import com.khartec.waltz.data.survey.SurveyQuestionDao;
import com.khartec.waltz.data.survey.SurveyQuestionResponseDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyInstance;
import com.khartec.waltz.model.survey.SurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.commons.jexl3.*;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.common.StringUtilities.isEmpty;

@Service
public class QuestionPredicateEvaluator {

    private final DSLContext dsl;
    private final SurveyQuestionDao questionDao;
    private final SurveyInstanceDao instanceDao;
    private final SurveyQuestionResponseDao responseDao;


    @Autowired
    public QuestionPredicateEvaluator(DSLContext dsl,
                                      SurveyQuestionDao questionDao,
                                      SurveyInstanceDao instanceDao,
                                      SurveyQuestionResponseDao responseDao) {
        this.dsl = dsl;
        this.questionDao = questionDao;
        this.instanceDao = instanceDao;
        this.responseDao = responseDao;
    }


    public List<SurveyQuestion> determineActiveQuestions(long surveyInstanceId) {
        List<SurveyQuestion> qs = loadQuestions(surveyInstanceId);
        Map<Long, SurveyQuestionResponse> responsesByQuestionId = loadResponses(surveyInstanceId);

        SurveyInstance instance = instanceDao.getById(surveyInstanceId);
        EntityReference subjectRef = instance.surveyEntity();
        return eval(qs, subjectRef, responsesByQuestionId);
    }


    private List<SurveyQuestion> eval(List<SurveyQuestion> qs,
                                      EntityReference subjectRef,
                                      Map<Long, SurveyQuestionResponse> responsesByQuestionId) {

        QuestionAppPredicateNamespace defaultNamespace = new QuestionAppPredicateNamespace(
                dsl,
                subjectRef,
                qs,
                responsesByQuestionId);

        JexlBuilder builder = new JexlBuilder();
        JexlEngine jexl = builder
                .namespaces(newHashMap(null, defaultNamespace))
                .create();

        defaultNamespace.usingEvaluator(jexl);

        return determineActiveQs(qs, jexl);
    }


    private static List<SurveyQuestion> determineActiveQs(List<SurveyQuestion> qs, JexlEngine jexl) {
        List<SurveyQuestion> activeQs = qs
                .stream()
                .filter(q -> q
                        .inclusionPredicate()
                        .map(p -> {
                            if (isEmpty(p)) {
                                return true;
                            } else {
                                JexlExpression expr = jexl.createExpression(p);
                                JexlContext jexlCtx = new MapContext();
                                Boolean result = Boolean.valueOf(expr.evaluate(jexlCtx).toString());
                                System.out.printf(
                                        "%s [%s] => %s\n",
                                        q.questionText(),
                                        q.inclusionPredicate().orElse(""),
                                        result);
                                return result;
                            }
                        })
                        .orElse(true))
                .collect(Collectors.toList());
        return activeQs;
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


    // --- TEST ---

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        QuestionPredicateEvaluator evaluator = ctx.getBean(QuestionPredicateEvaluator.class);

        long surveyInstanceId = 147L; // 95L;
        List<SurveyQuestion> activeQs = evaluator.determineActiveQuestions(surveyInstanceId);
        System.out.println("-------------");
        activeQs.forEach(System.out::println);
    }
}
