package com.khartec.waltz.data.scenario;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.ImmutableScenarioRatingItem;
import com.khartec.waltz.model.scenario.ScenarioRatingItem;
import com.khartec.waltz.schema.tables.records.ScenarioRatingItemRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.readRef;
import static com.khartec.waltz.schema.tables.ScenarioRatingItem.SCENARIO_RATING_ITEM;

@Repository
public class ScenarioRatingItemDao {

    private static final RecordMapper<? super Record, ScenarioRatingItem> TO_DOMAIN_MAPPER = r -> {
        ScenarioRatingItemRecord record = r.into(ScenarioRatingItemRecord.class);
        return ImmutableScenarioRatingItem.builder()
                .item(readRef(record, SCENARIO_RATING_ITEM.ITEM_KIND, SCENARIO_RATING_ITEM.ITEM_ID))
                .row(readRef(record, SCENARIO_RATING_ITEM.ROW_KIND, SCENARIO_RATING_ITEM.ROW_ID))
                .column(readRef(record, SCENARIO_RATING_ITEM.COLUMN_KIND, SCENARIO_RATING_ITEM.COLUMN_ID))
                .rating(record.getRating().charAt(0))
                .scenarioId(record.getScenarioId())
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
        SelectConditionStep<Record10<Long, Long, String, String, Long, String, Long, String, Timestamp, String>> originalData = DSL
                .select(
                        DSL.value(clonedScenarioId),
                        SCENARIO_RATING_ITEM.ITEM_ID,
                        SCENARIO_RATING_ITEM.ITEM_KIND,
                        SCENARIO_RATING_ITEM.RATING,
                        SCENARIO_RATING_ITEM.ROW_ID,
                        SCENARIO_RATING_ITEM.ROW_KIND,
                        SCENARIO_RATING_ITEM.COLUMN_ID,
                        SCENARIO_RATING_ITEM.COLUMN_KIND,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_AT,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_BY)
                .from(SCENARIO_RATING_ITEM)
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(command.scenarioId()));

        return dsl
                .insertInto(
                        SCENARIO_RATING_ITEM,
                        SCENARIO_RATING_ITEM.SCENARIO_ID,
                        SCENARIO_RATING_ITEM.ITEM_ID,
                        SCENARIO_RATING_ITEM.ITEM_KIND,
                        SCENARIO_RATING_ITEM.RATING,
                        SCENARIO_RATING_ITEM.ROW_ID,
                        SCENARIO_RATING_ITEM.ROW_KIND,
                        SCENARIO_RATING_ITEM.COLUMN_ID,
                        SCENARIO_RATING_ITEM.COLUMN_KIND,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_AT,
                        SCENARIO_RATING_ITEM.LAST_UPDATED_BY)
                .select(originalData)
                .execute();
    }

    public boolean remove(long scenarioId, long appId, long columnId, long rowId) {
        return dsl
                .deleteFrom(SCENARIO_RATING_ITEM)
                .where(SCENARIO_RATING_ITEM.ITEM_ID.eq(appId))
                .and(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(scenarioId))
                .and(SCENARIO_RATING_ITEM.ROW_ID.eq(rowId))
                .and(SCENARIO_RATING_ITEM.COLUMN_ID.eq(columnId))
                .execute() == 1;
    }


    public boolean add(long scenarioId, long appId, long columnId, long rowId, char rating, String userId) {
        return dsl
                .insertInto(SCENARIO_RATING_ITEM)
                .set(SCENARIO_RATING_ITEM.SCENARIO_ID, scenarioId)
                .set(SCENARIO_RATING_ITEM.ITEM_ID, appId)
                .set(SCENARIO_RATING_ITEM.ITEM_KIND, EntityKind.APPLICATION.name())
                .set(SCENARIO_RATING_ITEM.COLUMN_ID, columnId)
                .set(SCENARIO_RATING_ITEM.COLUMN_KIND, EntityKind.MEASURABLE.name())
                .set(SCENARIO_RATING_ITEM.ROW_ID, rowId)
                .set(SCENARIO_RATING_ITEM.ROW_KIND, EntityKind.MEASURABLE.name())
                .set(SCENARIO_RATING_ITEM.RATING, String.valueOf(rating))
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(SCENARIO_RATING_ITEM.LAST_UPDATED_BY, userId)
                .execute() == 1;
    }
}
