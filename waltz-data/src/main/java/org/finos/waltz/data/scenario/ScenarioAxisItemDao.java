/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.data.scenario;


import org.finos.waltz.model.AxisOrientation;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.scenario.CloneScenarioCommand;
import org.finos.waltz.model.scenario.ImmutableScenarioAxisItem;
import org.finos.waltz.model.scenario.ScenarioAxisItem;
import org.finos.waltz.schema.tables.records.ScenarioAxisItemRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.InlineSelectFieldFactory.mkNameField;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.schema.tables.ScenarioAxisItem.SCENARIO_AXIS_ITEM;

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
