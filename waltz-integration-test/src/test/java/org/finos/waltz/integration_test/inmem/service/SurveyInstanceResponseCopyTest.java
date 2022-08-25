package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.survey.SurveyInstanceService;
import org.finos.waltz.service.survey.SurveyRunService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.SurveyTemplateHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.Assert.assertEquals;

public class SurveyInstanceResponseCopyTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private SurveyTemplateHelper templateHelper;

    @Autowired
    private SurveyRunService runService;

    @Autowired
    private SurveyInstanceService instanceService;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;


    @Test
    public void copyResponses() {

        EntityReference a1 = appHelper.createNewApp(mkName("copyResponses"), ouIds.a);
        EntityReference a2 = appHelper.createNewApp(mkName("copyResponses"), ouIds.a);

        String username = mkName("copyResponses");
        Long personId = personHelper.createPerson(username);

        long templateId = templateHelper.createTemplate(username, mkName("copyResponses"));
        long qId = templateHelper.addQuestion(templateId);

        long invKindId = involvementHelper.mkInvolvementKind(mkName("invKind"));
        involvementHelper.createInvolvement(personId, invKindId, a1);
        involvementHelper.createInvolvement(personId, invKindId, a2);

        ImmutableSurveyRunCreateCommand runCmd = mkRunCommand(templateId, EntityReference.mkRef(EntityKind.ORG_UNIT, ouIds.a), invKindId);
        Long runId = runService.createSurveyRun(username, runCmd).id().get();

        InstancesAndRecipientsCreateCommand createCmd = mkInstancesRecipCreateCmd(runId);
        runService.createSurveyInstancesAndRecipients(createCmd);

        Set<SurveyInstance> instances = instanceService.findForSurveyRun(runId);

        SurveyInstance sourceSurvey = find(instances, d -> d.surveyEntity().id() == a1.id()).get();
        SurveyInstance targetSurvey = find(instances, d -> d.surveyEntity().id() == a2.id()).get();

        instanceService.saveResponse(username, sourceSurvey.id().get(), mkResponse(qId));

        ImmutableCopySurveyResponsesCommand copyCommand = ImmutableCopySurveyResponsesCommand.builder()
                .targetSurveyInstanceIds(ListUtilities.asList(targetSurvey.id().get()))
                .build();

        instanceService.copyResponses(sourceSurvey.id().get(), copyCommand,username);

        List<SurveyInstanceQuestionResponse> responses = instanceService.findResponses(targetSurvey.id().get());

        SurveyInstanceQuestionResponse q1StringResponse = find(responses, r -> r.questionResponse().questionId().equals(qId)).get();

        assertEquals("Can copy survey question responses to target instance",
                "yes",
                q1StringResponse.questionResponse().stringResponse().get());

        long q2Id = templateHelper.addQuestion(templateId);
        instanceService.saveResponse(username, sourceSurvey.id().get(), mkListResponse(q2Id));

        instanceService.copyResponses(sourceSurvey.id().get(), copyCommand,username);

        List<SurveyInstanceQuestionResponse> responsesWithListResponse = instanceService.findResponses(targetSurvey.id().get());
        SurveyInstanceQuestionResponse q2ListResponse = find(responsesWithListResponse, r -> r.questionResponse().questionId().equals(q2Id)).get();

        assertEquals("Can copy survey question list responses to target instance",
                asSet("yes", "no", "maybe"),
                fromCollection(q2ListResponse.questionResponse().listResponse().get()));

    }


    private SurveyQuestionResponse mkResponse(Long qId) {
        return ImmutableSurveyQuestionResponse.builder()
                .questionId(qId)
                .stringResponse("yes")
                .comment("comment")
                .build();
    }


    private SurveyQuestionResponse mkListResponse(Long qId) {
        return ImmutableSurveyQuestionResponse.builder()
                .questionId(qId)
                .listResponse(ListUtilities.asList("yes", "no", "maybe"))
                .comment("comment")
                .build();
    }

    private InstancesAndRecipientsCreateCommand mkInstancesRecipCreateCmd(Long runId) {
        return ImmutableInstancesAndRecipientsCreateCommand.builder()
                .surveyRunId(runId)
                .dueDate(toLocalDate(nowUtcTimestamp()))
                .approvalDueDate(toLocalDate(nowUtcTimestamp()))
                .excludedRecipients(emptySet())
                .build();
    }

    private ImmutableSurveyRunCreateCommand mkRunCommand(Long templateId, EntityReference selectorRef, Long invKindId) {
        return ImmutableSurveyRunCreateCommand.builder()
                .issuanceKind(SurveyIssuanceKind.GROUP)
                .name(mkName("test run"))
                .description("run desc")
                .selectionOptions(IdSelectionOptions.mkOpts(selectorRef))
                .surveyTemplateId(templateId)
                .addInvolvementKindIds(invKindId)
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .approvalDueDate(DateTimeUtilities.today().plusMonths(1))
                .contactEmail("someone@somewhere.com")
                .build();
    }
}
