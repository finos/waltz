package com.khartec.waltz.data.scenario;


import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.ImmutableScenarioAxisItem;
import com.khartec.waltz.model.scenario.ScenarioAxisItem;
import com.khartec.waltz.schema.tables.records.ScenarioAxisItemRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.InlineSelectFieldFactory.mkNameField;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.tables.ScenarioAxisItem.SCENARIO_AXIS_ITEM;

@Repository
public class ScenarioAxisItemDao {

    private static final Field<String> DOMAIN_ITEM_NAME_FIELD = mkNameField(
            SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID,
            SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND,
            newArrayList(EntityKind.MEASURABLE));


    private static final RecordMapper<? super Record, ScenarioAxisItem> TO_DOMAIN_MAPPER = r -> {
        ScenarioAxisItemRecord record = r.into(ScenarioAxisItemRecord.class);

        return ImmutableScenarioAxisItem.builder()
                .id(record.getId())
                .axisOrientation(AxisOrientation.valueOf(record.getOrientation() ))
                .position(record.getPosition())
                .scenarioId(record.getScenarioId())
                .domainItem(readRef(r, SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND, SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID, DOMAIN_ITEM_NAME_FIELD))
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
                .select(DOMAIN_ITEM_NAME_FIELD)
                .from(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(scenarioId))
                .orderBy(SCENARIO_AXIS_ITEM.POSITION, DOMAIN_ITEM_NAME_FIELD)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int cloneItems(CloneScenarioCommand command, Long clonedScenarioId) {
        SelectConditionStep<Record5<Long, Long, String, String, Integer>> originalData = DSL
                .select(
                    DSL.value(clonedScenarioId),
                    SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID,
                    SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND,
                    SCENARIO_AXIS_ITEM.ORIENTATION,
                    SCENARIO_AXIS_ITEM.POSITION)
                .from(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(command.scenarioId()));

        return dsl
                .insertInto(
                    SCENARIO_AXIS_ITEM,
                    SCENARIO_AXIS_ITEM.SCENARIO_ID,
                    SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID,
                    SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND,
                    SCENARIO_AXIS_ITEM.ORIENTATION,
                    SCENARIO_AXIS_ITEM.POSITION)
                .select(originalData)
                .execute();
    }


    public Boolean add(long scenarioId,
                       AxisOrientation orientation,
                       EntityReference domainItem,
                       Integer position) {
        ScenarioAxisItemRecord record = dsl.newRecord(SCENARIO_AXIS_ITEM);

        record.setScenarioId(scenarioId);
        record.setDomainItemId(domainItem.id());
        record.setDomainItemKind(domainItem.kind().name());
        record.setOrientation(orientation.name());
        record.setPosition(position);

        return record.store() == 1;
    }


    public Boolean remove(long scenarioId,
                          AxisOrientation orientation,
                          EntityReference domainItem) {
        return dsl
                .deleteFrom(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(scenarioId))
                .and(SCENARIO_AXIS_ITEM.ORIENTATION.eq(orientation.name()))
                .and(SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND.eq(domainItem.kind().name()))
                .and(SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID.eq(domainItem.id()))
                .execute() == 1;
    }


    public Collection<ScenarioAxisItem> findForScenarioAndOrientation(long scenarioId,
                                                                      AxisOrientation orientation) {
        return dsl
                .select(SCENARIO_AXIS_ITEM.fields())
                .select(DOMAIN_ITEM_NAME_FIELD)
                .from(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(scenarioId))
                .and(SCENARIO_AXIS_ITEM.ORIENTATION.eq(orientation.name()))
                .orderBy(SCENARIO_AXIS_ITEM.POSITION, DOMAIN_ITEM_NAME_FIELD)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int[] reorder(long scenarioId,
                         AxisOrientation orientation,
                         List<Long> orderedIds) {
        Collection<ScenarioAxisItemRecord> records = new ArrayList<>();

        for (int i = 0; i < orderedIds.size(); i++) {
            ScenarioAxisItemRecord record = dsl.newRecord(SCENARIO_AXIS_ITEM);
            record.setId(orderedIds.get(i));
            record.changed(SCENARIO_AXIS_ITEM.ID, false);
            record.setPosition(i * 10);
            records.add(record);
        }

        return dsl
                .batchUpdate(records)
                .execute();
    }

}
