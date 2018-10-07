package com.khartec.waltz.data.scenario;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.ImmutableScenario;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioStatus;
import com.khartec.waltz.schema.tables.records.ScenarioRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.*;
import static com.khartec.waltz.schema.tables.Scenario.SCENARIO;

@Repository
public class ScenarioDao {

    public static final RecordMapper<? super Record, Scenario> TO_DOMAIN_MAPPER = r -> {
        ScenarioRecord record = r.into(ScenarioRecord.class);
        return ImmutableScenario.builder()
                .id(record.getId())
                .name(record.getName())
                .roadmapId(record.getRoadmapId())
                .description(record.getDescription())
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getLifecycleStatus()))
                .scenarioStatus(ScenarioStatus.valueOf(record.getScenarioStatus()))
                .effectiveDate(toLocalDate(record.getEffectiveDate()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .build();
    };


    private static final BiFunction<Scenario, DSLContext, ScenarioRecord> TO_RECORD_MAPPER = (domainObj, dslContext) -> {
        ScenarioRecord record = dslContext.newRecord(SCENARIO);

        record.setRoadmapId(domainObj.roadmapId());
        record.setName(domainObj.name());
        record.setDescription(domainObj.description());
        record.setLifecycleStatus(domainObj.entityLifecycleStatus().name());
        record.setScenarioStatus(domainObj.scenarioStatus().name());
        record.setEffectiveDate(Date.valueOf(domainObj.effectiveDate()));
        record.setLastUpdatedBy(domainObj.lastUpdatedBy());
        record.setLastUpdatedAt(Timestamp.valueOf(domainObj.lastUpdatedAt()));

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public ScenarioDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Scenario getById(long id) {
        return dsl
                .select(SCENARIO.fields())
                .from(SCENARIO)
                .where(SCENARIO.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Collection<Scenario> findForRoadmapId(long roadmapId) {
        return dsl
                .select(SCENARIO.fields())
                .from(SCENARIO)
                .where(SCENARIO.ROADMAP_ID.eq(roadmapId))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public Collection<Scenario> findByRoadmapSelector(Select<Record1<Long>> selector) {
        return dsl
                .select(SCENARIO.fields())
                .from(SCENARIO)
                .where(SCENARIO.ROADMAP_ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Scenario cloneScenario(CloneScenarioCommand command) {
        Scenario orig = getById(command.scenarioId());

        Scenario clone = ImmutableScenario.copyOf(orig)
                .withName(command.newName())
                .withEntityLifecycleStatus(EntityLifecycleStatus.PENDING)
                .withLastUpdatedAt(nowUtc())
                .withLastUpdatedBy(command.userId());

        ScenarioRecord clonedRecord = TO_RECORD_MAPPER.apply(clone, dsl);
        clonedRecord.store();

        return ImmutableScenario
                .copyOf(clone)
                .withId(clonedRecord.getId());
    }


    public Boolean updateName(long scenarioId, String newValue, String userId) {
        return updateField(
                scenarioId,
                SCENARIO.NAME,
                newValue,
                userId) == 1;
    }


    public Boolean updateDescription(long scenarioId, String newValue, String userId) {
        return updateField(
                scenarioId,
                SCENARIO.DESCRIPTION,
                newValue,
                userId) == 1;
    }


    public Boolean updateEffectiveDate(long scenarioId, LocalDate newValue, String userId) {
        return updateField(
                scenarioId,
                SCENARIO.EFFECTIVE_DATE,
                toSqlDate(newValue),
                userId) == 1;
    }


    public Boolean updateLifecycleStatus(long scenarioId, EntityLifecycleStatus newValue, String userId) {
        return updateField(
                scenarioId,
                SCENARIO.LIFECYCLE_STATUS,
                newValue.name(),
                userId) == 1;
    }


    public Boolean updateScenarioStatus(long scenarioId, ScenarioStatus newValue, String userId) {
        return updateField(
                scenarioId,
                SCENARIO.SCENARIO_STATUS,
                newValue.name(),
                userId) == 1;
    }


    public Scenario add(long roadmapId, String name, String userId) {
        Scenario scenario = ImmutableScenario.builder()
                .roadmapId(roadmapId)
                .name(name)
                .description("")
                .entityLifecycleStatus(EntityLifecycleStatus.PENDING)
                .scenarioStatus(ScenarioStatus.INTERIM)
                .effectiveDate(today())
                .lastUpdatedAt(nowUtc())
                .lastUpdatedBy(userId)
                .build();

        ScenarioRecord record = TO_RECORD_MAPPER
                .apply(scenario, dsl);

        record.store();

        return ImmutableScenario
                .copyOf(scenario)
                .withId(record.getId());
    }


    // -- helpers --

    private <T> int updateField(long id, Field<T> field, T value, String userId) {
        return dsl
                .update(SCENARIO)
                .set(field, value)
                .set(SCENARIO.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(SCENARIO.LAST_UPDATED_BY, userId)
                .where(SCENARIO.ID.eq(id))
                .execute();
    }

}
