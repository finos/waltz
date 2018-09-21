package com.khartec.waltz.data.roadmap;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.roadmap.ImmutableScenario;
import com.khartec.waltz.model.roadmap.Scenario;
import com.khartec.waltz.schema.tables.records.ScenarioRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Scenario.SCENARIO;

@Repository
public class ScenarioDao {

    private static final RecordMapper<? super Record, Scenario> TO_DOMAIN_MAPPER = r -> {
        ScenarioRecord record = r.into(ScenarioRecord.class);
        return ImmutableScenario.builder()
                .id(record.getId())
                .name(record.getName())
                .roadmapId(record.getRoadmapId())
                .status(ReleaseLifecycleStatus.valueOf(record.getLifecycleStatus()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .build();
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
}
