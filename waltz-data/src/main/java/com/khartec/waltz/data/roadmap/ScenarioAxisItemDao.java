package com.khartec.waltz.data.roadmap;


import com.khartec.waltz.model.roadmap.AxisKind;
import com.khartec.waltz.model.roadmap.ImmutableScenarioAxisItem;
import com.khartec.waltz.model.roadmap.ScenarioAxisItem;
import com.khartec.waltz.schema.tables.records.ScenarioAxisItemRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.tables.ScenarioAxisItem.SCENARIO_AXIS_ITEM;

@Repository
public class ScenarioAxisItemDao {


    private static final RecordMapper<? super Record, ScenarioAxisItem> TO_DOMAIN_MAPPER = r -> {
        ScenarioAxisItemRecord record = r.into(ScenarioAxisItemRecord.class);

        return ImmutableScenarioAxisItem.builder()
                .id(record.getId())
                .axisKind(AxisKind.valueOf(record.getAxisKind()))
                .order(record.getPosition())
                .scenarioId(record.getScenarioId())
                .item(readRef(record, SCENARIO_AXIS_ITEM.ITEM_KIND, SCENARIO_AXIS_ITEM.ITEM_ID))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ScenarioAxisItemDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }

    public Collection<ScenarioAxisItem> findForScenarioId(long scenarioId) {
        return dsl
                .select(SCENARIO_AXIS_ITEM.fields())
                .from(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(scenarioId))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
