package org.finos.waltz.service.survey;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.survey.SurveyInstanceActionQueueDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceStatusChangeCommand;
import org.finos.waltz.model.survey.SurveyInstance;
import org.finos.waltz.model.survey.SurveyInstanceActionParams;
import org.finos.waltz.model.survey.SurveyInstanceActionQueueItem;
import org.finos.waltz.model.survey.SurveyInstanceActionStatus;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class SurveyInstanceActionQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstanceActionQueueService.class);
    private final SurveyInstanceActionQueueDao surveyInstanceActionQueueDao;
    private final SurveyInstanceService surveyInstanceService;
    private final ChangeLogService changeLogService;
    private final DSLContext dslContext;


    @Autowired
    public SurveyInstanceActionQueueService(SurveyInstanceActionQueueDao surveyInstanceActionQueueDao,
                                     SurveyInstanceService surveyInstanceService,
                                     ChangeLogService changeLogService,
                                     DSLContext dslContext) {

        Checks.checkNotNull(surveyInstanceActionQueueDao, "surveyInstanceActionQueueDao cannot be null");
        Checks.checkNotNull(surveyInstanceService, "surveyInstanceService cannot be null");
        Checks.checkNotNull(changeLogService, "changeLogService cannot be null");
        Checks.checkNotNull(dslContext, "dslContext cannot be null");

        this.surveyInstanceActionQueueDao = surveyInstanceActionQueueDao;
        this.surveyInstanceService = surveyInstanceService;
        this.changeLogService = changeLogService;
        this.dslContext = dslContext;
    }

    /**
     * Looks for any 'PENDING' actions in the survey_instance_action_queue and attempts to run them in order of submission time to the queue.
     * In case of error or a precondition failure a message is saved to the action in the table.
     * A transaction is created for each action and all changes will be rolled back if an error occurs during runtime.
     */
    public void performActions() {

        List<SurveyInstanceActionQueueItem> pendingActions = surveyInstanceActionQueueDao.findPendingActions();

        pendingActions
                .forEach(action -> {

                    dslContext.transaction(ctx -> {

                        DSLContext tx = ctx.dsl();

                        Long actionId = action.id().get();

                        SurveyInstance instance = surveyInstanceService.getById(action.surveyInstanceId());

                        surveyInstanceActionQueueDao.markActionInProgress(tx, actionId);

                        if (instance == null) {

                            String msg = format("Failed to apply queued action: %s. Could not find survey instance with id: %d", action.action().name(), action.surveyInstanceId());

                            LOG.info(msg);
                            surveyInstanceActionQueueDao.updateActionStatus(
                                    tx,
                                    actionId,
                                    SurveyInstanceActionStatus.PRECONDITION_FAILURE,
                                    msg);

                            ChangeLog changeLog = mkChangelogForAction(tx, action, msg);
                            changeLogService.write(Optional.of(tx), changeLog);

                        } else if (instance.status() != action.initialState()) {

                            String msg = format("Failed to apply queued action: %s to survey: %d. Initial state of survey is not as expected: %s and is actually %s",
                                    action.action().name(),
                                    action.surveyInstanceId(),
                                    action.initialState().name(),
                                    instance.status().name());

                            LOG.info(msg);
                            surveyInstanceActionQueueDao.updateActionStatus(
                                    tx,
                                    actionId,
                                    SurveyInstanceActionStatus.PRECONDITION_FAILURE,
                                    msg);

                            ChangeLog changeLog = mkChangelogForAction(tx, action, msg);
                            changeLogService.write(Optional.of(tx), changeLog);

                        } else {

                            String username = action.submittedBy();

                            Optional<String> reason = action.actionParams().flatMap(SurveyInstanceActionParams::reason);
                            Optional<LocalDate> dueDate = action.actionParams().flatMap(SurveyInstanceActionParams::newDueDate);
                            Optional<LocalDate> approvalDueDate = action.actionParams().flatMap(SurveyInstanceActionParams::newApprovalDueDate);

                            ImmutableSurveyInstanceStatusChangeCommand updateCmd = ImmutableSurveyInstanceStatusChangeCommand
                                    .builder()
                                    .action(action.action())
                                    .reason(reason)
                                    .newDueDate(dueDate)
                                    .newApprovalDueDate(approvalDueDate)
                                    .build();

                            try {

                                // We need a new transaction here so that any changes get rolled back; we do not fail the overall
                                // action transaction as that resets the action to 'PENDING' and would lose the error message

                                tx.transaction(actionCtx -> {
                                    DSLContext actionTx = actionCtx.dsl();
                                    SurveyInstanceStatus surveyInstanceStatus = surveyInstanceService.updateStatus(
                                            Optional.of(actionTx),
                                            username,
                                            action.surveyInstanceId(),
                                            updateCmd);

                                    String msg = format("Successfully applied queued action: %s to survey: %d. New status is: %s",
                                            action.action().name(),
                                            action.surveyInstanceId(),
                                            surveyInstanceStatus.name());

                                    LOG.info(msg);
                                    surveyInstanceActionQueueDao.updateActionStatus(
                                            tx,
                                            actionId,
                                            SurveyInstanceActionStatus.SUCCESS,
                                            null);

                                    ChangeLog changeLog = mkChangelogForAction(tx, action, msg);
                                    changeLogService.write(Optional.of(tx), changeLog);
                                });


                            } catch (Exception e) {

                                String msg = format("Failed to apply queued action: %s to survey: %d. Error when updating: %s",
                                        action.action().name(),
                                        action.surveyInstanceId(),
                                        e.getMessage());

                                LOG.error(msg);
                                surveyInstanceActionQueueDao.updateActionStatus(
                                        tx,
                                        actionId,
                                        SurveyInstanceActionStatus.EXECUTION_FAILURE,
                                        msg);

                                ChangeLog changeLog = mkChangelogForAction(tx, action, msg);
                                changeLogService.write(Optional.of(tx), changeLog);
                            }
                        }

                    });

                });
    }


    private ChangeLog mkChangelogForAction(DSLContext tx, SurveyInstanceActionQueueItem action, String msg) {
        return ImmutableChangeLog
                .builder()
                .message(msg)
                .userId(action.submittedBy())
                .parentReference(mkRef(EntityKind.SURVEY_INSTANCE, action.surveyInstanceId()))
                .operation(Operation.UPDATE)
                .severity(Severity.INFORMATION)
                .build();
    }


    public List<SurveyInstanceActionQueueItem> findPendingActions() {
        return surveyInstanceActionQueueDao.findPendingActions();
    }

    public SurveyInstanceActionQueueItem getById(long id) {
        return surveyInstanceActionQueueDao.getById(id);
    }

}
