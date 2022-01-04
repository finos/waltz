package org.finos.waltz.jobs.harness;

import org.finos.waltz.data.survey.SurveyQuestionResponseDao;
import org.finos.waltz.model.survey.SurveyInstanceFormDetails;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.survey.SurveyInstanceEvaluator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SurveyInstanceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SurveyInstanceEvaluator evaluator = ctx.getBean(SurveyInstanceEvaluator.class);
        SurveyQuestionResponseDao responseDao = ctx.getBean(SurveyQuestionResponseDao.class);

        long surveyInstanceId = 107L; // 95L;
        SurveyInstanceFormDetails formDetails = evaluator.eval(surveyInstanceId);


        System.out.println("-------------");
        formDetails.activeQs().forEach(q -> System.out.printf("Q:%d / %s\n", q.id().get(), q.questionText()));
        System.out.println("Missing: " + formDetails.missingMandatoryQuestions());
    }
}
