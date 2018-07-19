/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.logical_flow;

import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlowMeasures;
import com.khartec.waltz.model.logical_flow.LogicalFlowMeasures;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.ImmutableTallyPack;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.tally.TallyPack;
import com.khartec.waltz.schema.tables.LogicalFlow;
import com.khartec.waltz.schema.tables.LogicalFlowDecorator;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.safeGet;
import static com.khartec.waltz.model.EntityKind.DATA_TYPE;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.*;
import static org.jooq.lambda.tuple.Tuple.tuple;


@Repository
public class LogicalFlowStatsDao {

    private final DSLContext dsl;
    private static final LogicalFlow lf = LOGICAL_FLOW.as("lf");

    private static final com.khartec.waltz.schema.tables.LogicalFlowDecorator lfd = LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR.as("lfd");

    private static final Condition BOTH_APPS =
            lf.SOURCE_ENTITY_KIND.eq(inline(EntityKind.APPLICATION.name()))
                .and(lf.TARGET_ENTITY_KIND.eq(inline(EntityKind.APPLICATION.name())));

    private static final Condition NOT_REMOVED = lf.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());

    private final DBExecutorPoolInterface dbExecutorPool;


    @Autowired
    public LogicalFlowStatsDao(DSLContext dsl, DBExecutorPoolInterface dbExecutorPool) {
        checkNotNull(dsl, "dsl must not be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");

        this.dsl = dsl;
        this.dbExecutorPool = dbExecutorPool;
    }


    public LogicalFlowMeasures countDistinctAppInvolvementByAppIdSelector(Select<Record1<Long>> appIdSelector) {

        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Select<Record1<Integer>> inAppCounter = countDistinctApps(
                    appIdSelector,
                    lf.TARGET_ENTITY_ID,
                    lf.SOURCE_ENTITY_ID
        );

        Select<Record1<Integer>> outAppCounter = countDistinctApps(
                    appIdSelector,
                    lf.SOURCE_ENTITY_ID,
                    lf.TARGET_ENTITY_ID
        );

        Select<Record1<Integer>> intraAppCounter = dsl
                    .select(count())
                    .from(APPLICATION)
                    .where(dsl.renderInlined(APPLICATION.ID.in(appIdSelector)));

        Future<Integer> inAppCount = dbExecutorPool.submit(() -> inAppCounter.fetchOne().value1());
        Future<Integer> outAppCount = dbExecutorPool.submit(() -> outAppCounter.fetchOne().value1());
        Future<Integer> intraAppCount = dbExecutorPool.submit(() -> intraAppCounter.fetchOne().value1());

        Supplier<ImmutableLogicalFlowMeasures> appCountSupplier = Unchecked.supplier(() -> ImmutableLogicalFlowMeasures.builder()
                .inbound(inAppCount.get())
                .outbound(outAppCount.get())
                .intra(intraAppCount.get())
                .build());

        return appCountSupplier.get();
    }


    public List<TallyPack<String>> tallyDataTypesByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Table<Record1<Long>> sourceApp = appIdSelector.asTable("source_app");
        Table<Record1<Long>> targetApp = appIdSelector.asTable("target_app");
        Field<Long> sourceAppId = sourceApp.field(0, Long.class);
        Field<Long> targetAppId = targetApp.field(0, Long.class);
        Field<Integer> flowCount = DSL.field("flow_count", Integer.class);
        Field<String> flowTypeCase =
                when(sourceAppId.isNotNull()
                        .and(targetAppId.isNotNull()), inline("INTRA"))
                .when(sourceAppId.isNotNull(), inline("OUTBOUND"))
                .otherwise(inline("INBOUND"));
        Field<String> flowType = DSL.field("flow_type", String.class);

        Condition condition = sourceAppId
                .isNotNull()
                .or(targetAppId.isNotNull())
                .and(BOTH_APPS)
                .and(NOT_REMOVED);

        Result<Record3<Long, String, Integer>> records = dsl.select(
                    lfd.DECORATOR_ENTITY_ID,
                    flowTypeCase.as(flowType),
                    count().as(flowCount))
                .from(lf)
                .innerJoin(lfd)
                    .on(lf.ID.eq(lfd.LOGICAL_FLOW_ID)
                        .and(lfd.DECORATOR_ENTITY_KIND.eq(inline(DATA_TYPE.name()))))
                .leftJoin(sourceApp)
                    .on(sourceAppId.eq(lf.SOURCE_ENTITY_ID))
                .leftJoin(targetApp)
                    .on(targetAppId.eq(lf.TARGET_ENTITY_ID))
                .where(dsl.renderInlined(condition))
                .groupBy(lfd.DECORATOR_ENTITY_ID, flowTypeCase)
                .fetch();

        Map<EntityReference, List<Tally<String>>> dataTypeRefToTallies = records.stream()
                .map(r -> tuple(
                        mkRef(EntityKind.DATA_TYPE, r.getValue(lfd.DECORATOR_ENTITY_ID)),
                        ImmutableTally.<String>builder()
                                .id(r.getValue(flowType))
                                .count(r.getValue(flowCount))
                                .build()))
                .collect(groupingBy(t -> t.v1(),
                            mapping(t -> t.v2(),
                                    toList())));

        return dataTypeRefToTallies.entrySet()
                .stream()
                .map(e -> ImmutableTallyPack.<String>builder()
                        .entityReference(e.getKey())
                        .tallies(e.getValue())
                        .build())
                .collect(toList());
    }


    public LogicalFlowMeasures countDistinctFlowInvolvementByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        checkNotNull(appIdSelector, "appIdSelector cannot be null");

        Table<Record1<Long>> sourceApp = appIdSelector.asTable("source_app");
        Table<Record1<Long>> targetApp = appIdSelector.asTable("target_app");
        Field<Long> sourceAppId = sourceApp.field(0, Long.class);
        Field<Long> targetAppId = targetApp.field(0, Long.class);

        Field<BigDecimal> inboundCount = DSL.sum(
                DSL.when(sourceAppId.isNull()
                        .and(targetAppId.isNotNull()), inline(1))
                    .otherwise(inline(0)))
                .as("inbound_count");

        Field<BigDecimal> outboundCount = DSL.sum(
                DSL.when(sourceAppId.isNotNull()
                        .and(targetAppId.isNull()), inline(1))
                    .otherwise(inline(0)))
                .as("outbound_count");

        Field<BigDecimal> intraCount = DSL.sum(
                DSL.when(sourceAppId.isNotNull()
                        .and(targetAppId.isNotNull()), inline(1))
                    .otherwise(inline(0)))
                .as("intra_count");

        Record3<BigDecimal, BigDecimal, BigDecimal> counts = dsl.select(
                    inboundCount,
                    outboundCount,
                    intraCount)
                .from(lf)
                .leftJoin(sourceApp)
                    .on(sourceAppId.eq(lf.SOURCE_ENTITY_ID))
                .leftJoin(targetApp)
                    .on(targetAppId.eq(lf.TARGET_ENTITY_ID))
                .where(BOTH_APPS)
                .and(NOT_REMOVED)
                .fetchAny();

        return ImmutableLogicalFlowMeasures.builder()
                .inbound(safeGet(counts, inboundCount, BigDecimal.ZERO).doubleValue())
                .outbound(safeGet(counts, outboundCount, BigDecimal.ZERO).doubleValue())
                .intra(safeGet(counts, intraCount, BigDecimal.ZERO).doubleValue())
                .build();
    }



    // -- App Counts

    private SelectConditionStep<Record1<Integer>> countDistinctApps(Select<Record1<Long>> appIdSelector,
                                                                            Field<Long> feederField,
                                                                            Field<Long> fieldToCount) {
        Condition condition = fieldToCount
                .notIn(appIdSelector)
                .and(feederField.in(appIdSelector))
                .and(BOTH_APPS)
                .and(NOT_REMOVED);

        return dsl.select(DSL.countDistinct(fieldToCount))
                .from(lf)
                .where(dsl.renderInlined(condition));

    }

}
