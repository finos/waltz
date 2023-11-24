package org.finos.waltz.integration_test.inmem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.JacksonUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ReleaseLifecycleStatus;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.survey.ImmutableInstancesAndRecipientsCreateCommand;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceActionParams;
import org.finos.waltz.model.survey.ImmutableSurveyQuestionResponse;
import org.finos.waltz.model.survey.ImmutableSurveyRunCreateCommand;
import org.finos.waltz.model.survey.InstancesAndRecipientsCreateCommand;
import org.finos.waltz.model.survey.SurveyInstance;
import org.finos.waltz.model.survey.SurveyInstanceAction;
import org.finos.waltz.model.survey.SurveyInstanceActionParams;
import org.finos.waltz.model.survey.SurveyInstanceActionQueueItem;
import org.finos.waltz.model.survey.SurveyInstanceActionStatus;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.model.survey.SurveyIssuanceKind;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.survey.SurveyInstanceActionQueueService;
import org.finos.waltz.service.survey.SurveyInstanceService;
import org.finos.waltz.service.survey.SurveyRunService;
import org.finos.waltz.test_common.helpers.ActionQueueHelper;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.SurveyTemplateHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.any;
import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class SurveyInstanceActionQueueTest extends BaseInMemoryIntegrationTest {

    public static final String REASON_TEXT = "test reason to capture";

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private SurveyTemplateHelper templateHelper;

    @Autowired
    private SurveyRunService runService;

    @Autowired
    private SurveyInstanceService instanceService;

    @Autowired
    private SurveyInstanceActionQueueService actionQueueService;

    @Autowired
    private ActionQueueHelper actionQueueHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private ChangeLogService changeLogService;

    @Test
    public void processActionsCanSubmitSurveys() throws InsufficientPrivelegeException {
        String username = mkName("submitSurvey");
        SurveyInstance instance = setupSurvey("submitSurvey", username);
        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(1, pendingActions.size(), "One action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.COMPLETED, instancePostAction.status(), "After submitting action the survey should be marked as completed");
    }

    @Test
    public void processActionsCanHandleMultipleActions() throws InsufficientPrivelegeException {
        String username = mkName("multipleActions");
        SurveyInstance instance = setupSurvey("multipleActions", username);
        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.APPROVING, "Reason given", SurveyInstanceStatus.COMPLETED, username);
        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.REOPENING, null, SurveyInstanceStatus.APPROVED, username);
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(3, pendingActions.size(), "Three actions should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.IN_PROGRESS, instancePostAction.status(), "Survey instance should be 'in progress' after submission, approval and reopen");
    }

    @Test
    public void processActionsWillOrderBySubmittedTimeAndOnlyActionFirstIfDuplicates() throws InsufficientPrivelegeException {
        String username = mkName("multipleActions");
        SurveyInstance instance = setupSurvey("multipleActions", username);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        Long action2Id = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        Long action3Id = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        Long action4Id = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        Long action5Id = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(5, pendingActions.size(), "Two actions should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.COMPLETED, instancePostAction.status(), "Survey instance should be 'completed' after submission");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        SurveyInstanceActionQueueItem action2 = actionQueueService.getById(action2Id);
        SurveyInstanceActionQueueItem action3 = actionQueueService.getById(action3Id);
        SurveyInstanceActionQueueItem action4 = actionQueueService.getById(action4Id);
        SurveyInstanceActionQueueItem action5 = actionQueueService.getById(action5Id);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, action.status(), "First action to be submitted should be successful");
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action2.status(), "Duplicate actions should be ignored as survey initial status changed");
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action3.status(), "Duplicate actions should be ignored as survey initial status changed");
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action4.status(), "Duplicate actions should be ignored as survey initial status changed");
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action5.status(), "Duplicate actions should be ignored as survey initial status changed");
    }

    @Test
    public void processActionsCannotSubmitIfMandatoryQuestionsOutstanding() throws InsufficientPrivelegeException {
        String username = mkName("mandatoryQuestion");
        SurveyInstance instance = setupSurvey("mandatoryQuestion", username, false);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), username);
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(1, pendingActions.size(), "One action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.NOT_STARTED, instancePostAction.status(), "After submitting action the status should not have changed as mandatory questions outstanding");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        Assertions.assertEquals(SurveyInstanceActionStatus.EXECUTION_FAILURE, action.status());
        Assertions.assertNotNull(action.message());
    }

    @Test
    public void processActionsCannotSubmitIfNotPermitted() throws InsufficientPrivelegeException {
        String username = mkName("notPermitted");
        SurveyInstance instance = setupSurvey("notPermitted", username);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, instance.status(), "unauthorisedUser");
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(1, pendingActions.size(), "One action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.NOT_STARTED, instancePostAction.status(), "After submitting action the status should not have changed as mandatory questions outstanding");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        Assertions.assertEquals(SurveyInstanceActionStatus.EXECUTION_FAILURE, action.status());
        Assertions.assertNotNull(action.message());
    }

    @Test
    public void processActionsCannotSubmitSurveyInstanceStatusIncorrect() throws InsufficientPrivelegeException {
        String username = mkName("surveyStatusMismatch");
        SurveyInstance instance = setupSurvey("surveyStatusMismatch", username);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, SurveyInstanceStatus.IN_PROGRESS, username);
        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();

        Assertions.assertEquals(1, pendingActions.size(), "One action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.NOT_STARTED, instancePostAction.status(), "After submitting action the status should not have changed as mandatory questions outstanding");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action.status());
        Assertions.assertNotNull(action.message());
    }

    @Test
    public void processActionsFailureOfOneActionDoesNotStopOthersCompleting() throws InsufficientPrivelegeException {
        String username = mkName("multipleSurveys");
        String username2 = mkName("multipleSurveys");
        SurveyInstance instance = setupSurvey("multipleSurveys", username);
        SurveyInstance instance2 = setupSurvey("multipleSurveys", username2);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, SurveyInstanceStatus.COMPLETED, username);
        Long actionId2 = actionQueueHelper.addActionToQueue(instance2.id().get(), SurveyInstanceAction.SUBMITTING, null, instance2.status(), username2);

        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();
        Assertions.assertEquals(2, pendingActions.size(), "Two action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.NOT_STARTED, instancePostAction.status(), "Survey should not have been completed, should remain 'not started'");

        SurveyInstance instance2PostAction = instanceService.getById(instance2.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.COMPLETED, instance2PostAction.status(), "Survey should be completed");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        Assertions.assertEquals(SurveyInstanceActionStatus.PRECONDITION_FAILURE, action.status());
        Assertions.assertNotNull(action.message());

        SurveyInstanceActionQueueItem action2 = actionQueueService.getById(actionId2);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, action2.status());
    }

    @Test
    public void processActionsShouldInformWhenTransitionIsNotAllowed() throws InsufficientPrivelegeException {
        String username = mkName("multipleSurveys");
        SurveyInstance instance = setupSurvey("multipleSurveys", username);
        Long actionId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.APPROVING, null, SurveyInstanceStatus.NOT_STARTED, username);

        List<SurveyInstanceActionQueueItem> pendingActions = actionQueueService.findPendingActions();
        Assertions.assertEquals(1, pendingActions.size(), "Two action should exist in the queue before processing");

        actionQueueService.performActions();

        List<SurveyInstanceActionQueueItem> pendingActionsPostProcess = actionQueueService.findPendingActions();
        Assertions.assertEquals(0, pendingActionsPostProcess.size(), "No actions should exist in the queue after processing");

        SurveyInstance instancePostAction = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.NOT_STARTED, instancePostAction.status(), "Action should not have been completed, should remain 'not started'");

        SurveyInstanceActionQueueItem action = actionQueueService.getById(actionId);
        Assertions.assertEquals(SurveyInstanceActionStatus.EXECUTION_FAILURE, action.status());
        Assertions.assertNotNull(action.message());
    }

    @Test
    public void processActionsCanDoAllActions() throws InsufficientPrivelegeException {
        String username = mkName("rejected");
        SurveyInstance instance = setupSurvey("rejected", username);

        // Rejection
        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, SurveyInstanceStatus.NOT_STARTED, username);
        Long rejectedId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.REJECTING, null, SurveyInstanceStatus.COMPLETED, username);

        actionQueueService.performActions();

        SurveyInstance postRejection = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.REJECTED, postRejection.status(), "Survey should be marked 'rejected'");

        SurveyInstanceActionQueueItem rejected = actionQueueService.getById(rejectedId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, rejected.status());

        // Withdraw
        Long withdrawalId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.WITHDRAWING, null, SurveyInstanceStatus.REJECTED, username);
        actionQueueService.performActions();

        SurveyInstance postWithdrawal = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.WITHDRAWN, postWithdrawal.status(), "Survey should be marked 'withdrawn'");

        SurveyInstanceActionQueueItem withdrawal = actionQueueService.getById(withdrawalId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, withdrawal.status());

        // Reopen
        Long reopenId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.REOPENING, null, SurveyInstanceStatus.WITHDRAWN, username);
        actionQueueService.performActions();

        SurveyInstance postReopen = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.IN_PROGRESS, postReopen.status(), "Survey should be marked 'in progress'");

        SurveyInstanceActionQueueItem reopened = actionQueueService.getById(reopenId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, reopened.status());

        // Submit
        Long submitId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, SurveyInstanceStatus.IN_PROGRESS, username);
        actionQueueService.performActions();

        SurveyInstance postSubmit = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.COMPLETED, postSubmit.status(), "Action should be marked 'completed'");

        SurveyInstanceActionQueueItem submit = actionQueueService.getById(submitId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, submit.status());

        // Approve
        Long approvalId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.APPROVING, null, SurveyInstanceStatus.COMPLETED, username);
        actionQueueService.performActions();

        SurveyInstance postApproval = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.APPROVED, postApproval.status(), "Action should be marked 'approved'");

        SurveyInstanceActionQueueItem approval = actionQueueService.getById(approvalId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, approval.status());
    }


    @Test
    public void processActionsAbleToParseReasonFromAction() throws InsufficientPrivelegeException {
        String username = mkName("reason");
        SurveyInstance instance = setupSurvey("reason", username);

        actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.SUBMITTING, null, SurveyInstanceStatus.NOT_STARTED, username);

        ImmutableSurveyInstanceActionParams reasonParams = ImmutableSurveyInstanceActionParams
                .builder()
                .reason(REASON_TEXT)
                .build();

        String reason = mkReasonString(reasonParams);

        Long approvalId = actionQueueHelper.addActionToQueue(instance.id().get(), SurveyInstanceAction.APPROVING, reason, SurveyInstanceStatus.COMPLETED, username);
        actionQueueService.performActions();

        SurveyInstance postApproval = instanceService.getById(instance.id().get());
        Assertions.assertEquals(SurveyInstanceStatus.APPROVED, postApproval.status(), "Action should be marked 'approved'");

        SurveyInstanceActionQueueItem approval = actionQueueService.getById(approvalId);
        Assertions.assertEquals(SurveyInstanceActionStatus.SUCCESS, approval.status());

        List<ChangeLog> changeLogs = changeLogService.findByParentReference(mkRef(EntityKind.SURVEY_INSTANCE, postApproval.id().get()), Optional.empty(), Optional.empty());
        Assertions.assertTrue(any(changeLogs, d -> d.message().contains(REASON_TEXT)), "Reason should be captured in the change log");
    }

    private String mkReasonString(SurveyInstanceActionParams reason) {
        try {
            return JacksonUtilities.getJsonMapper().writeValueAsString(reason);
        } catch (JsonProcessingException e) {
            return null;
        }
    }


    private SurveyInstance setupSurvey(String nameStem, String username) throws InsufficientPrivelegeException {
        return setupSurvey(nameStem, username, true);
    }

    private SurveyInstance setupSurvey(String nameStem, String username, boolean populateResponses) throws InsufficientPrivelegeException {

        EntityReference a1 = appHelper.createNewApp(mkName(nameStem), ouIds.a);
        Long personId = personHelper.createPerson(username);

        long templateId = templateHelper.createTemplate(username, mkName(nameStem));
        long qId = templateHelper.addMandatoryQuestion(templateId);
        long q2Id = templateHelper.addQuestion(templateId);

        templateHelper.updateStatus(username, templateId, ReleaseLifecycleStatus.ACTIVE);

        long invKindId = involvementHelper.mkInvolvementKind(mkName("invKind"));
        involvementHelper.createInvolvement(personId, invKindId, a1);

        ImmutableSurveyRunCreateCommand runCmd = mkRunCommand(templateId, mkRef(EntityKind.ORG_UNIT, ouIds.a), invKindId);
        Long runId = runService.createSurveyRun(username, runCmd).id().get();

        InstancesAndRecipientsCreateCommand createCmd = mkInstancesRecipCreateCmd(runId);
        runService.createSurveyInstancesAndRecipients(createCmd);

        Set<SurveyInstance> instances = instanceService.findForSurveyRun(runId);

        SurveyInstance sourceSurvey = find(instances, d -> d.surveyEntity().id() == a1.id()).get();

        if (populateResponses) {
            instanceService.saveResponse(username, sourceSurvey.id().get(), mkResponse(qId));
            instanceService.saveResponse(username, sourceSurvey.id().get(), mkResponse(q2Id));
        }

        return sourceSurvey;
    }


    private SurveyQuestionResponse mkResponse(Long qId) {
        return ImmutableSurveyQuestionResponse.builder()
                .questionId(qId)
                .stringResponse("yes")
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
                .addOwnerInvKindIds(invKindId)
                .dueDate(DateTimeUtilities.today().plusMonths(1))
                .approvalDueDate(DateTimeUtilities.today().plusMonths(1))
                .contactEmail("someone@somewhere.com")
                .build();
    }
}
