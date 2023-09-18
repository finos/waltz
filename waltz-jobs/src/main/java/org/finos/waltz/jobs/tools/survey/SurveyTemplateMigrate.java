package org.finos.waltz.jobs.tools.survey;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.survey.SurveyInstanceDao;
import org.finos.waltz.data.survey.SurveyInstanceRecipientDao;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceCreateCommand;
import org.finos.waltz.model.survey.SurveyInstance;
import org.finos.waltz.model.survey.SurveyInstanceCreateCommand;
import org.finos.waltz.service.DIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.model.survey.SurveyInstanceStatus.*;

@Component
public class SurveyTemplateMigrate {
    private static final Logger LOG = LoggerFactory.getLogger(SurveyTemplateMigrate.class);
    private static final long OLD_TEMPLATE_ID = 111;
    private static final long NEW_TEMPLATE_ID = 183;
    private static final long NEW_RUN_ID = 48082;


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SurveyInstanceDao siDao = ctx.getBean(SurveyInstanceDao.class);
        SurveyInstanceRecipientDao siRecipientDao = ctx.getBean(SurveyInstanceRecipientDao.class);

        Set<SurveyInstance> oldSurveyInstances = siDao.findForSurveyTemplate(OLD_TEMPLATE_ID, NOT_STARTED, IN_PROGRESS);
        LOG.info("Got {} survey instances from existing template id [{}]", oldSurveyInstances.size(), OLD_TEMPLATE_ID);

        // survey instance ids for items to NOT migrate
        Set<Long> exceptionsToNotMigrate = SetUtilities.asSet(110279L, 109550L,  110325L,  109789L,  108233L,  110311L,  110310L,  110315L,  108131L,  110308L,  110314L,  107042L,  109420L,  110307L,  110312L,  106656L,  105630L,  110306L,  110380L);

        Set<SurveyInstance> toMigrate = oldSurveyInstances.stream()
                .filter(si -> !exceptionsToNotMigrate.contains(si.id().get()))
                .collect(Collectors.toSet());

        // create survey instances
        for (SurveyInstance si : toMigrate) {
            LocalDate newIssueDate = DateTimeUtilities.nowUtc().toLocalDate();

            SurveyInstanceCreateCommand newSiCreateCommand = ImmutableSurveyInstanceCreateCommand.builder()
                    .surveyRunId(NEW_RUN_ID)
                    .entityReference(si.surveyEntity())
                    .dueDate(newIssueDate.plusDays(30))
                    .approvalDueDate(newIssueDate.plusDays(60))
                    .name(si.name())
                    .status(NOT_STARTED)
                    .owningRole(si.owningRole())
                    .issuedOn(newIssueDate)
                    .build();

            long newSiId = siDao.create(newSiCreateCommand);
            LOG.info("Created new survey instance [id: {}], cmd: {}", newSiId, newSiCreateCommand);
            List<Person> recipients = siRecipientDao.findPeopleForSurveyInstance(si.id().get());
            siDao.createInstanceRecipients(newSiId, ListUtilities.map(recipients, r -> r.id().get()));
            LOG.info("Added recipients to new survey instance [id: {}]", newSiId);

            // withdraw the old one
            siDao.updateStatus(si.id().get(), WITHDRAWN);
            LOG.info("Old survey instance [id: {}] withdrawn", si.id().get());
        }

    }
}
