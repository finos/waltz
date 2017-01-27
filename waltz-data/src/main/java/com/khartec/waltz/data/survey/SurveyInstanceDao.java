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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;

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


    public void deleteForSurveyRun(long surveyRunId) {
        dsl.delete(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId));
    }
}
