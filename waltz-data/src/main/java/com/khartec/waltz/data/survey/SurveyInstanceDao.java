package com.khartec.waltz.data.survey;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.ImmutableSurveyInstance;
import com.khartec.waltz.model.survey.SurveyInstance;
import com.khartec.waltz.model.survey.SurveyInstanceCreateCommand;
import com.khartec.waltz.model.survey.SurveyInstanceStatus;
import com.khartec.waltz.schema.tables.records.SurveyInstanceRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE_RECIPIENT;

@Repository
public class SurveyInstanceDao {

    private static final RecordMapper<Record, SurveyInstance> TO_DOMAIN_MAPPER = r -> {
        SurveyInstanceRecord record = r.into(SURVEY_INSTANCE);
        return ImmutableSurveyInstance.builder()
                .id(record.getId())
                .surveyRunId(record.getSurveyRunId())
                .surveyEntity(EntityReference.mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .status(SurveyInstanceStatus.valueOf(record.getStatus()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public SurveyInstance getById(long id) {
        return dsl.select()
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findForSurveyRunAndRecipient(long surveyRunId, long personId) {
        return dsl.select()
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                    .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId)
                        .and(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId)))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long create(SurveyInstanceCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SurveyInstanceRecord record = dsl.newRecord(SURVEY_INSTANCE);
        record.setSurveyRunId(command.surveyRunId());
        record.setEntityKind(command.entityReference().kind().name());
        record.setEntityId(command.entityReference().id());
        record.setStatus(command.status().name());

        record.store();
        return record.getId();
    }


    public int deleteForSurveyRun(long surveyRunId) {
        return dsl.delete(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .execute();
    }


    public int updateStatus(long instanceId, SurveyInstanceStatus newStatus) {
        checkNotNull(newStatus, "newStatus cannot be null");

        return dsl.update(SURVEY_INSTANCE)
                .set(SURVEY_INSTANCE.STATUS, newStatus.name())
                .where(SURVEY_INSTANCE.STATUS.notEqual(newStatus.name())
                        .and(SURVEY_INSTANCE.ID.eq(instanceId)))
                .execute();
    }
}
