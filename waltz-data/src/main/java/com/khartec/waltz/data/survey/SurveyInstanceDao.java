package com.khartec.waltz.data.survey;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.schema.tables.records.SurveyInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE_RECIPIENT;

@Repository
public class SurveyInstanceDao {

    private static final Field<String> ENTITY_NAME_FIELD = mkEntityNameField(
                SURVEY_INSTANCE.ENTITY_ID,
                SURVEY_INSTANCE.ENTITY_KIND,
                newArrayList(EntityKind.values()))
            .as("entity_name");


    private static final RecordMapper<Record, SurveyInstance> TO_DOMAIN_MAPPER = r -> {
        SurveyInstanceRecord record = r.into(SURVEY_INSTANCE);
        return ImmutableSurveyInstance.builder()
                .id(record.getId())
                .surveyRunId(record.getSurveyRunId())
                .surveyEntity(EntityReference.mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
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
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findForRecipient(long personId) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_INSTANCE_RECIPIENT)
                .on(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID))
                .where(SURVEY_INSTANCE_RECIPIENT.PERSON_ID.eq(personId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstance> findForSurveyRun(long surveyRunId) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
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


    public List<SurveyInstance> findBySurveyInstanceIdSelector(Select<Record1<Long>> selector) {
        return dsl.select(SURVEY_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public SurveyRunCompletionRate getCompletionRateForSurveyRun(long surveyRunId) {
        final Result<Record2<String, Integer>> countsByStatus = dsl.select(SURVEY_INSTANCE.STATUS, DSL.count(SURVEY_INSTANCE.ID))
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .groupBy(SURVEY_INSTANCE.STATUS)
                .fetch();

        return ImmutableSurveyRunCompletionRate.builder()
                .notStartedCount(getCountByStatus(countsByStatus, SurveyInstanceStatus.NOT_STARTED))
                .inProgressCount(getCountByStatus(countsByStatus, SurveyInstanceStatus.IN_PROGRESS))
                .completedCount(getCountByStatus(countsByStatus, SurveyInstanceStatus.COMPLETED))
                .expiredCount(getCountByStatus(countsByStatus, SurveyInstanceStatus.EXPIRED))
                .build();
    }


    private int getCountByStatus(Result<Record2<String, Integer>> countsByStatus, SurveyInstanceStatus status) {
        return countsByStatus.stream()
                .filter(r -> status.name().equals(r.value1()))
                .findAny()
                .map(Record2::value2)
                .orElse(0);

    }
}
