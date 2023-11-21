package org.finos.waltz.service.survey;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.survey.SurveyInstanceActionQueueDao;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceStatusChangeCommand;
import org.finos.waltz.model.survey.SurveyInstance;
import org.finos.waltz.model.survey.SurveyInstanceActionQueueItem;
import org.finos.waltz.model.survey.SurveyInstanceActionStatus;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class SurveyInstanceActionQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstanceActionQueueService.class);
    private final SurveyInstanceActionQueueDao surveyInstanceActionQueueDao;
    private final SurveyInstanceService surveyInstanceService;
    private final DSLContext dslContext;

    @Autowired
    SurveyInstanceActionQueueService(SurveyInstanceActionQueueDao surveyInstanceActionQueueDao,
                                     SurveyInstanceService surveyInstanceService,
                                     DSLContext dslContext) {

        Checks.checkNotNull(surveyInstanceActionQueueDao, "surveyInstanceActionQueueDao cannot be null");
        Checks.checkNotNull(surveyInstanceService, "surveyInstanceService cannot be null");
        Checks.checkNotNull(dslContext, "dslContext cannot be null");

        this.surveyInstanceActionQueueDao = surveyInstanceActionQueueDao;
        this.surveyInstanceService = surveyInstanceService;
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

                            String msg = format("Could not find survey instance with id: %d to apply action: %s", action.surveyInstanceId(), action.action().name());
                            LOG.info(msg);
                            surveyInstanceActionQueueDao.updateActionStatus(
                                    tx,
                                    actionId,
                                    SurveyInstanceActionStatus.PRECONDITION_FAILURE,
                                    msg);

                        } else if (instance.status() != action.initialState()) {

                            String msg = format("Initial state of survey instance with id: %d is not as expected: %s and is actually %s, will not apply action: %s",
                                    action.surveyInstanceId(),
                                    action.initialState().name(),
                                    instance.status().name(),
                                    action.action().name());
                            LOG.info(msg);
                            surveyInstanceActionQueueDao.updateActionStatus(
                                    tx,
                                    actionId,
                                    SurveyInstanceActionStatus.PRECONDITION_FAILURE,
                                    msg);

                        } else {

                            String username = action.submittedBy();

                            ImmutableSurveyInstanceStatusChangeCommand updateCmd = ImmutableSurveyInstanceStatusChangeCommand
                                    .builder()
                                    .action(action.action())
                                    .reason(Optional.ofNullable(action.actionParams()))
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

                                    String msg = format("Successfully updated survey instance id: %d with action: %s, new status is: %s",
                                            action.surveyInstanceId(),
                                            action.action().name(),
                                            surveyInstanceStatus.name());

                                    LOG.info(msg);
                                    surveyInstanceActionQueueDao.updateActionStatus(
                                            tx,
                                            actionId,
                                            SurveyInstanceActionStatus.SUCCESS,
                                            null);
                                });


                            } catch (Exception e) {

                                String msg = format("Error when updating survey instance id: %d with action: %s. %s",
                                        action.surveyInstanceId(),
                                        action.action().name(),
                                        e.getMessage());

                                LOG.error(msg);
                                surveyInstanceActionQueueDao.updateActionStatus(
                                        tx,
                                        actionId,
                                        SurveyInstanceActionStatus.EXECUTION_FAILURE,
                                        msg);
                            }
                        }

                    });

                });
    }


    public List<SurveyInstanceActionQueueItem> findPendingActions() {
        return surveyInstanceActionQueueDao.findPendingActions();
    }

    public SurveyInstanceActionQueueItem getById(long id) {
        return surveyInstanceActionQueueDao.getById(id);
    }

}
