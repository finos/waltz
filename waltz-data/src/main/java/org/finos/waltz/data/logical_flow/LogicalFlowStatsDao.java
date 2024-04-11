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

package org.finos.waltz.data.logical_flow;

import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.*;
import org.finos.waltz.data.DBExecutorPoolInterface;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.logical_flow.*;
import org.finos.waltz.model.tally.ImmutableTally;
import org.finos.waltz.model.tally.ImmutableTallyPack;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.model.tally.TallyPack;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Record3;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityKind.DATA_TYPE;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.FlowDirection.INBOUND;
import static org.finos.waltz.model.FlowDirection.OUTBOUND;
import static org.jooq.impl.DSL.*;


@Repository
public class LogicalFlowStatsDao {

    private final DSLContext dsl;
    private static final LogicalFlow lf = LOGICAL_FLOW.as("lf");
    private static final org.finos.waltz.schema.tables.LogicalFlowDecorator lfd = LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR.as("lfd");
    private static final Application counterpart_app = APPLICATION.as("counterpart_app");
    private static final Actor counterpart_actor = ACTOR.as("counterpart_actor");
    private static final EndUserApplication counterpart_euc = END_USER_APPLICATION.as("counterpart_euc");
    private static final DataType rollup_dt = Tables.DATA_TYPE.as("rollup_dt");
    private static final DataType actual_dt = Tables.DATA_TYPE.as("actual_dt");

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

        Map<EntityReference, List<Tally<String>>> dataTypeRefToTallies  = dsl
                .select(
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
                .fetchGroups(
                        r -> mkRef(EntityKind.DATA_TYPE, r.getValue(lfd.DECORATOR_ENTITY_ID)),
                        r -> ImmutableTally.<String>builder()
                                .id(r.getValue(flowType))
                                .count(r.getValue(flowCount))
                                .build());

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
                .inbound(JooqUtilities.safeGet(counts, inboundCount, BigDecimal.ZERO).doubleValue())
                .outbound(JooqUtilities.safeGet(counts, outboundCount, BigDecimal.ZERO).doubleValue())
                .intra(JooqUtilities.safeGet(counts, intraCount, BigDecimal.ZERO).doubleValue())
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


    public Map<FlowDirection, Set<FlowInfo>> getFlowInfoByDirection(EntityReference ref, Long datatypeId){

        Condition sourceAppCondition = lf.SOURCE_ENTITY_ID.eq(counterpart_app.ID).and(lf.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
        Condition sourceActorCondition = lf.SOURCE_ENTITY_ID.eq(counterpart_actor.ID).and(lf.SOURCE_ENTITY_KIND.eq(EntityKind.ACTOR.name()));
        Condition sourceEucCondition = lf.SOURCE_ENTITY_ID.eq(counterpart_euc.ID).and(lf.SOURCE_ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()));
        Condition targetAppCondition = lf.TARGET_ENTITY_ID.eq(counterpart_app.ID).and(lf.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
        Condition targetActorCondition = lf.TARGET_ENTITY_ID.eq(counterpart_actor.ID).and(lf.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name()));
        Condition targetEucCondition = lf.TARGET_ENTITY_ID.eq(counterpart_euc.ID).and(lf.TARGET_ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()));

        Condition dataTypeCondition = datatypeId == null ? rollup_dt.PARENT_ID.isNull() : rollup_dt.PARENT_ID.eq(datatypeId);

        Condition isUpstream = lf.SOURCE_ENTITY_ID.eq(ref.id()).and(lf.SOURCE_ENTITY_KIND.eq(ref.kind().name()));
        Condition isDownstream = lf.TARGET_ENTITY_ID.eq(ref.id()).and(lf.TARGET_ENTITY_KIND.eq(ref.kind().name()));

        SelectConditionStep<Record11<Long, Long, String, Long, Long, String, String, Long, String, String, String>> sourceQry = mkDirectionalQuery(
                sourceAppCondition,
                sourceActorCondition,
                sourceEucCondition,
                dataTypeCondition,
                isDownstream,
                INBOUND);

        SelectConditionStep<Record11<Long, Long, String, Long, Long, String, String, Long, String, String, String>> targetQry = mkDirectionalQuery(
                targetAppCondition,
                targetActorCondition,
                targetEucCondition,
                dataTypeCondition,
                isUpstream,
                OUTBOUND);

        SelectOrderByStep<Record11<Long, Long, String, Long, Long, String, String, Long, String, String, String>> unionedData =
                sourceQry
                        .union(targetQry);

        return unionedData
                .fetch()
                .stream()
                .collect(groupingBy(
                        record -> {
                            String directionStr = record.get("direction", String.class);
                            return FlowDirection.valueOf(directionStr);
                        },
                        mapping(r -> {

                            EntityReference rollupDtRef = mkRef(DATA_TYPE, r.get(rollup_dt.ID), r.get(rollup_dt.NAME));
                            EntityReference actualDtRef = mkRef(DATA_TYPE, r.get(actual_dt.ID), r.get(actual_dt.NAME));

                            EntityReference counterpartRef = mkRef(
                                    EntityKind.valueOf(r.get("counterpart_kind", String.class)),
                                    r.get("counterpart_id", Long.class),
                                    r.get("counterpart_name", String.class));

                            return (FlowInfo) ImmutableFlowInfo.builder()
                                    .classificationId(r.get(FLOW_CLASSIFICATION.ID))
                                    .rollupDataType(rollupDtRef)
                                    .actualDataType(actualDtRef)
                                    .counterpart(counterpartRef)
                                    .flowEntityLifecycleStatus(EntityLifecycleStatus.valueOf(r.get(lf.ENTITY_LIFECYCLE_STATUS)))
                                    .flowId(r.get(lf.ID))
                                    .build();

                        }, toSet())));
    }


    private SelectConditionStep<Record11<Long, Long, String, Long, Long, String, String, Long, String, String, String>> mkDirectionalQuery(Condition appDirectionCondition,
                                                                                                                                           Condition actorDirectionCondition,
                                                                                                                                           Condition eucDirectionCondition,
                                                                                                                                           Condition dataTypeCondition,
                                                                                                                                           Condition parentDirection,
                                                                                                                                           FlowDirection direction) {

        return dsl
                .select(lf.ID,
                        rollup_dt.ID,
                        rollup_dt.NAME,
                        FLOW_CLASSIFICATION.ID,
                        actual_dt.ID,
                        actual_dt.NAME,
                        DSL.when(counterpart_app.ID.isNotNull(), DSL.val(EntityKind.APPLICATION.name()))
                            .when(counterpart_actor.ID.isNotNull(), DSL.val(EntityKind.ACTOR.name()))
                            .when(counterpart_euc.ID.isNotNull(), DSL.val(EntityKind.END_USER_APPLICATION.name()))
                            .as("counterpart_kind"),
                        DSL.coalesce(counterpart_app.ID, counterpart_actor.ID, counterpart_euc.ID).as("counterpart_id"),
                        lf.ENTITY_LIFECYCLE_STATUS,
                        DSL.coalesce(counterpart_app.NAME, counterpart_actor.NAME, counterpart_euc.NAME).as("counterpart_name"),
                        DSL.val(direction.name()).as("direction"))
                .from(lf)
                .innerJoin(lfd).on(lf.ID.eq(lfd.LOGICAL_FLOW_ID))
                .innerJoin(ENTITY_HIERARCHY).on(lfd.DECORATOR_ENTITY_ID.eq(ENTITY_HIERARCHY.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(DATA_TYPE.name())
                                .and(lfd.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name()))))
                .innerJoin(rollup_dt).on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(rollup_dt.ID))
                .innerJoin(actual_dt).on(ENTITY_HIERARCHY.ID.eq(actual_dt.ID))
                .innerJoin(FLOW_CLASSIFICATION).on(lfd.RATING.eq(FLOW_CLASSIFICATION.CODE))
                .leftJoin(counterpart_app).on(appDirectionCondition)
                .leftJoin(counterpart_actor).on(actorDirectionCondition)
                .leftJoin(counterpart_euc).on(eucDirectionCondition)
                .where(NOT_REMOVED)
                .and(dataTypeCondition)
                .and(parentDirection);
    }

}
