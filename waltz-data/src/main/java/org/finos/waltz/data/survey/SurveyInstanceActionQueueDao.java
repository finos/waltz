package org.finos.waltz.data.survey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceActionQueueItem;
import org.finos.waltz.model.survey.SurveyInstanceAction;
import org.finos.waltz.model.survey.SurveyInstanceActionParams;
import org.finos.waltz.model.survey.SurveyInstanceActionQueueItem;
import org.finos.waltz.model.survey.SurveyInstanceActionStatus;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.schema.tables.records.SurveyInstanceActionQueueRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE_ACTION_QUEUE;

@Repository
public class SurveyInstanceActionQueueDao {

    private final DSLContext dsl;

    public static final RecordMapper<Record, SurveyInstanceActionQueueItem> TO_DOMAIN_MAPPER = r -> {

        SurveyInstanceActionQueueRecord record = r.into(SURVEY_INSTANCE_ACTION_QUEUE);

        Optional<SurveyInstanceActionParams> surveyInstanceActionParams = readParams(getJsonMapper(), record.getActionParams());

        return ImmutableSurveyInstanceActionQueueItem.builder()
                .id(record.getId())
                .action(SurveyInstanceAction.valueOf(record.getAction()))
                .surveyInstanceId(record.getSurveyInstanceId())
                .actionParams(surveyInstanceActionParams)
                .initialState(SurveyInstanceStatus.valueOf(record.getInitialState()))
                .submittedAt(DateTimeUtilities.toLocalDateTime(record.getSubmittedAt()))
                .submittedBy(record.getSubmittedBy())
                .actionedAt(ofNullable(record.getActionedAt()).map(Timestamp::toLocalDateTime).orElse(null))
                .status(SurveyInstanceActionStatus.valueOf(record.getStatus()))
                .message(record.getMessage())
                .provenance(record.getProvenance())
                .build();
    };


    private static Optional<SurveyInstanceActionParams> readParams(ObjectMapper jsonMapper, String actionParams) {
        if(actionParams == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.ofNullable(jsonMapper.readValue(actionParams, SurveyInstanceActionParams.class));
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        }
    }


    @Autowired
    public SurveyInstanceActionQueueDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<SurveyInstanceActionQueueItem> findPendingActions() {
        Condition isPending = SURVEY_INSTANCE_ACTION_QUEUE.STATUS.eq(SurveyInstanceActionStatus.PENDING.name());
        return mkSelectByCondition(dsl, isPending)
                .orderBy(SURVEY_INSTANCE_ACTION_QUEUE.SUBMITTED_AT)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public SurveyInstanceActionQueueItem getById(long id) {
        Condition idCondition = SURVEY_INSTANCE_ACTION_QUEUE.ID.eq(id);
        return mkSelectByCondition(dsl, idCondition)
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    private SelectConditionStep<Record> mkSelectByCondition(DSLContext dslContext, Condition condition) {
        return dslContext
                .select(SURVEY_INSTANCE_ACTION_QUEUE.fields())
                .from(SURVEY_INSTANCE_ACTION_QUEUE)
                .where(condition);
    }


    public void updateActionStatus(DSLContext tx, Long actionId, SurveyInstanceActionStatus instanceActionStatus, String msg) {
        int updated = tx
                .update(SURVEY_INSTANCE_ACTION_QUEUE)
                .set(SURVEY_INSTANCE_ACTION_QUEUE.ACTIONED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(SURVEY_INSTANCE_ACTION_QUEUE.STATUS, instanceActionStatus.name())
                .set(SURVEY_INSTANCE_ACTION_QUEUE.MESSAGE, msg)
                .where(SURVEY_INSTANCE_ACTION_QUEUE.ID.eq(actionId)
                        .and(SURVEY_INSTANCE_ACTION_QUEUE.STATUS.eq(SurveyInstanceActionStatus.IN_PROGRESS.name())))
                .execute();

        if (updated != 1) {
            String messageString = "Unable to update action queue item with id: %d as %d records were updated. " +
                    "Reverting all action changes, this action will be attempted again in future as will be rolled back to 'PENDING'";

            throw new IllegalStateException(format(
                    messageString,
                    actionId,
                    updated));
        }
    }


    public void markActionInProgress(DSLContext tx, Long actionId) {

        SelectConditionStep<Record1<Long>> inProgressAction = DSL
                .select(SURVEY_INSTANCE_ACTION_QUEUE.ID)
                .from(SURVEY_INSTANCE_ACTION_QUEUE)
                .where(SURVEY_INSTANCE_ACTION_QUEUE.STATUS.eq(SurveyInstanceActionStatus.IN_PROGRESS.name()));

        int updated = tx
                .update(SURVEY_INSTANCE_ACTION_QUEUE)
                .set(SURVEY_INSTANCE_ACTION_QUEUE.STATUS, SurveyInstanceActionStatus.IN_PROGRESS.name())
                .where(SURVEY_INSTANCE_ACTION_QUEUE.ID.eq(actionId)
                        .and(SURVEY_INSTANCE_ACTION_QUEUE.STATUS.eq(SurveyInstanceActionStatus.PENDING.name()))
                        .and(DSL.notExists(inProgressAction)))
                .execute();

        if (updated != 1) {

            String messageString = "Unable to mark action %d as 'IN_PROGRESS', either the action id was not found, the action is no longer pending or there is another action currently marked 'IN_PROGRESS'";

            throw new IllegalStateException(format(
                    messageString,
                    actionId,
                    updated));
        }
    }
}
