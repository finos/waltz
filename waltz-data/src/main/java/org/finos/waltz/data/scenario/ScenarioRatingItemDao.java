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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.scenario.ChangeScenarioCommand;
import org.finos.waltz.model.scenario.CloneScenarioCommand;
import org.finos.waltz.model.scenario.ImmutableScenarioRatingItem;
import org.finos.waltz.model.scenario.ScenarioRatingItem;
import org.finos.waltz.schema.tables.records.ScenarioRatingItemRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record11;
import org.jooq.RecordMapper;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.finos.waltz.schema.tables.Scenario.SCENARIO;
import static org.finos.waltz.schema.tables.ScenarioRatingItem.SCENARIO_RATING_ITEM;

@Repository
public class ScenarioRatingItemDao {

    private static final RecordMapper<? super Record, ScenarioRatingItem> TO_DOMAIN_MAPPER = r -> {
        ScenarioRatingItemRecord record = r.into(ScenarioRatingItemRecord.class);
        return ImmutableScenarioRatingItem.builder()
                .item(readRef(record, SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND, SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID))
                .row(readRef(record, SCENARIO_RATING_ITEM.ROW_KIND, SCENARIO_RATING_ITEM.ROW_ID))
                .column(readRef(record, SCENARIO_RATING_ITEM.COLUMN_KIND, SCENARIO_RATING_ITEM.COLUMN_ID))
                .rating(record.getRating().charAt(0))
                .scenarioId(record.getScenarioId())
                .description(record.getDescription())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ScenarioRatingItemDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<ScenarioRatingItem> findForScenarioId(long scenarioId) {
        return dsl
                .select(SCENARIO_RATING_ITEM.fields())
                .from(SCENARIO_RATING_ITEM)
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(scenarioId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int cloneItems(CloneScenarioCommand command, Long clonedScenarioId) {
        SelectConditionStep<Record11<Long, Long, String, String, Long, String, Long, String, String, Timestamp, String>> originalData = DSL
                .select(
                        DSL.value(clonedScenarioId),
                        SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID,
                        SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND,
                        SCENARIO_RATING_ITEM.RATING,
                        SCENARIO_RATING_ITEM.ROW_ID,
                        SCENARIO_RATING_ITEM.ROW_KIND,
                        SCENARIO_RATING_ITEM.COLUMN_ID,
                        SCENARIO_RATING_ITEM.COLUMN_KIND,
                        SCENARIO_RATING_ITEM.DESCRIPTION,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_AT,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_BY)
                .from(SCENARIO_RATING_ITEM)
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(command.scenarioId()));

        return dsl
                .insertInto(
                        SCENARIO_RATING_ITEM,
                        SCENARIO_RATING_ITEM.SCENARIO_ID,
                        SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID,
                        SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND,
                        SCENARIO_RATING_ITEM.RATING,
                        SCENARIO_RATING_ITEM.ROW_ID,
                        SCENARIO_RATING_ITEM.ROW_KIND,
                        SCENARIO_RATING_ITEM.COLUMN_ID,
                        SCENARIO_RATING_ITEM.COLUMN_KIND,
                        SCENARIO_RATING_ITEM.DESCRIPTION,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_AT,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_BY)
                .select(originalData)
                .execute();
    }


    public boolean remove(ChangeScenarioCommand command, String userId) {

        boolean rc = dsl
                .deleteFrom(SCENARIO_RATING_ITEM)
                .where(mkCoordinatesCondition(command))
                .execute() == 1;

        if (rc) {
            updateScenarioTimestamp(command.scenarioId(), userId);
        }

        return rc;
    }


    public boolean add(ChangeScenarioCommand command, String userId) {

        boolean rc = dsl
                .insertInto(SCENARIO_RATING_ITEM)
                .set(SCENARIO_RATING_ITEM.SCENARIO_ID, command.scenarioId())
                .set(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID, command.appId())
                .set(SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND, EntityKind.APPLICATION.name())
                .set(SCENARIO_RATING_ITEM.COLUMN_ID, command.columnId())
                .set(SCENARIO_RATING_ITEM.COLUMN_KIND, EntityKind.MEASURABLE.name())
                .set(SCENARIO_RATING_ITEM.ROW_ID, command.rowId())
                .set(SCENARIO_RATING_ITEM.ROW_KIND, EntityKind.MEASURABLE.name())
                .set(SCENARIO_RATING_ITEM.RATING, String.valueOf(command.rating()))
                .set(SCENARIO_RATING_ITEM.DESCRIPTION, "")
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_BY, userId)
                .execute() == 1;

        if (rc) {
            updateScenarioTimestamp(command.scenarioId(), userId);
        }

        return rc;
    }


    public boolean updateRating(ChangeScenarioCommand command, String userId) {
        boolean rc = dsl
                .update(SCENARIO_RATING_ITEM)
                .set(SCENARIO_RATING_ITEM.RATING, String.valueOf(command.rating()))
                .set(SCENARIO_RATING_ITEM.DESCRIPTION, command.comment())
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_BY, userId)
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .where(mkCoordinatesCondition(command))
                .execute() == 1;

        if (rc) {
            updateScenarioTimestamp(command.scenarioId(), userId);
        }

        return rc;
    }


    // -- helpers

    private Condition mkCoordinatesCondition(ChangeScenarioCommand command) {
        return SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID.eq(command.appId())
                .and(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(command.scenarioId()))
                .and(SCENARIO_RATING_ITEM.ROW_ID.eq(command.rowId()))
                .and(SCENARIO_RATING_ITEM.COLUMN_ID.eq(command.columnId()));
    }


    private void updateScenarioTimestamp(long scenarioId, String userId) {
        dsl.update(SCENARIO)
                .set(SCENARIO.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(SCENARIO.LAST_UPDATED_BY, userId)
                .where(SCENARIO.ID.eq(scenarioId))
                .execute();
    }

}
