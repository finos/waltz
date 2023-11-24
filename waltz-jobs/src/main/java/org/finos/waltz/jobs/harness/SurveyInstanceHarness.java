package org.finos.waltz.jobs.harness;

import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.survey.SurveyInstanceActionQueueService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SurveyInstanceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        SurveyInstanceActionQueueService svc = ctx.getBean(SurveyInstanceActionQueueService.class);

        svc.performActions();

        System.out.println("------------- Done!");
    }
}
