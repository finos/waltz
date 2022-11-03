package org.finos.waltz.jobs.harness;

import org.finos.waltz.data.survey.SurveyInstanceDao;
import org.finos.waltz.data.survey.SurveyQuestionResponseDao;
import org.finos.waltz.model.attestation.SyncRecipientsResponse;
import org.finos.waltz.model.survey.SurveyInstanceFormDetails;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.survey.SurveyInstanceEvaluator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SurveyInstanceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        SurveyInstanceDao dao = ctx.getBean(SurveyInstanceDao.class);

        SyncRecipientsResponse reassignRecipientsCounts = dao.getReassignRecipientsCounts();

        dao.reassignRecipients();

        System.out.println("-------------");
    }
}
