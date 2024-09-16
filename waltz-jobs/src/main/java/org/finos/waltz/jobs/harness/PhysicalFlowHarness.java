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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.schema.tables.PhysicalSpecDataType;
import org.finos.waltz.schema.tables.PhysicalSpecification;
import org.finos.waltz.service.DIConfiguration;
import org.immutables.value.Value;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record10;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class PhysicalFlowHarness {

    @Value.Immutable
    interface LogicalFlowKey {
        long lfId();
        EntityReference src();
        EntityReference trg();
    }

    @Value.Immutable
    interface PhysicalFlowKey {
        long lfId();
        long pfId();
        @Nullable
        String flowName();

        @Nullable
        String transport();
        @Nullable
        String flowExtId();
        @Nullable
        String specName();
        @Nullable
        String specExtId();

        @Nullable
        String specDescription();

        @Nullable
        String flowDescription();
    }


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        PhysicalFlow pf = Tables.PHYSICAL_FLOW;
        PhysicalSpecification ps = Tables.PHYSICAL_SPECIFICATION;
        PhysicalSpecDataType psdt = Tables.PHYSICAL_SPEC_DATA_TYPE;
        LogicalFlow lf = Tables.LOGICAL_FLOW;
        DataType dt = Tables.DATA_TYPE;
        LogicalFlowDecorator lfd = Tables.LOGICAL_FLOW_DECORATOR;

        Condition lfIsActive =  lf.IS_REMOVED.isFalse().and(lf.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));
        Condition pfIsActive =  pf.IS_REMOVED.isFalse().and(pf.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()));

        Collection<EntityKind> kinds = SetUtilities.asSet(EntityKind.APPLICATION, EntityKind.END_USER_APPLICATION, EntityKind.ACTOR);
        Field<String> srcName = InlineSelectFieldFactory.mkNameField(lf.SOURCE_ENTITY_ID, lf.SOURCE_ENTITY_KIND, kinds);
        Field<String> trgName = InlineSelectFieldFactory.mkNameField(lf.TARGET_ENTITY_ID, lf.TARGET_ENTITY_KIND, kinds);
        Field<String> srcExtId = InlineSelectFieldFactory.mkExternalIdField(lf.SOURCE_ENTITY_ID, lf.SOURCE_ENTITY_KIND, kinds);
        Field<String> trgExtId = InlineSelectFieldFactory.mkExternalIdField(lf.TARGET_ENTITY_ID, lf.TARGET_ENTITY_KIND, kinds);

        SelectConditionStep<Record10<Long, String, Long, String, String, String, Long, String, String, String>> lfQry = dsl
                .select(lf.ID, lf.SOURCE_ENTITY_KIND, lf.SOURCE_ENTITY_ID, srcName, srcExtId,
                        lf.TARGET_ENTITY_KIND, lf.TARGET_ENTITY_ID, trgName, trgExtId,
                        dt.NAME)
                .from(lf)
                .innerJoin(lfd).on(lfd.LOGICAL_FLOW_ID.eq(lf.ID))
                .innerJoin(dt).on(dt.ID.eq(lfd.DECORATOR_ENTITY_ID).and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .where(lfIsActive);

        SelectConditionStep<Record10<Long, Long, String, String, String, String, String, String, String, String>> pfQry = dsl
                .select(lf.ID,
                        pf.ID, pf.NAME, ps.NAME, pf.EXTERNAL_ID, pf.TRANSPORT,
                        ps.EXTERNAL_ID,
                        dt.NAME,
                        ps.DESCRIPTION, pf.DESCRIPTION)
                .from(lf)
                .innerJoin(pf).on(pf.LOGICAL_FLOW_ID.eq(lf.ID))
                .innerJoin(ps).on(ps.ID.eq(pf.SPECIFICATION_ID))
                .innerJoin(psdt).on(psdt.SPECIFICATION_ID.eq(ps.ID))
                .innerJoin(dt).on(dt.ID.eq(psdt.DATA_TYPE_ID))
                .where(lfIsActive.and(pfIsActive));

        Map<LogicalFlowKey, List<String>> lfsToDTs = MapUtilities
                .groupAndThen(
                        lfQry.fetch(),
                        r -> ImmutableLogicalFlowKey.builder()
                                .lfId(r.get(lf.ID))
                                .src(EntityReference.mkRef(EntityKind.valueOf(r.get(lf.SOURCE_ENTITY_KIND)), r.get(lf.SOURCE_ENTITY_ID), r.get(srcName), null, r.get(srcExtId)))
                                .trg(EntityReference.mkRef(EntityKind.valueOf(r.get(lf.TARGET_ENTITY_KIND)), r.get(lf.TARGET_ENTITY_ID), r.get(trgName), null, r.get(trgExtId)))
                                .build(),
                        xs -> xs.stream().map(r -> r.get(dt.NAME)).distinct().sorted().collect(Collectors.toList()));

        Map<PhysicalFlowKey, List<String>> pfsToDTs = MapUtilities
                .groupAndThen(
                        pfQry.fetch(),
                        r -> ImmutablePhysicalFlowKey
                                .builder()
                                .lfId(r.get(lf.ID))
                                .pfId(r.get(pf.ID))
                                .flowName(r.get(pf.NAME))
                                .flowExtId(r.get(pf.EXTERNAL_ID))
                                .transport(r.get(pf.TRANSPORT))
                                .specName(r.get(ps.NAME))
                                .specExtId(r.get(ps.EXTERNAL_ID))
                                .flowDescription(r.get(pf.DESCRIPTION))
                                .specDescription(r.get(ps.DESCRIPTION))
                                .build(),
                        xs -> xs.stream().map(r -> r.get(dt.NAME)).distinct().sorted().collect(Collectors.toList()));

        Map<Long, Collection<Tuple2<PhysicalFlowKey, List<String>>>> lfToPfs = MapUtilities
                .groupBy(
                        pfsToDTs.entrySet(),
                        kv -> kv.getKey().lfId(),
                        kv -> tuple(kv.getKey(), kv.getValue()));

        List<Tuple4<LogicalFlowKey, List<String>, PhysicalFlowKey, List<String>>> big = lfsToDTs
                .entrySet()
                .stream()
                .map(kv -> tuple(kv.getKey(), kv.getValue()))
                .flatMap(t -> {
                    Collection<Tuple2<PhysicalFlowKey, List<String>>> pfs = lfToPfs.get(t.v1.lfId());
                    return pfs == null
                            ? Stream.of(t.concat(
                                    tuple((PhysicalFlowKey) null, (List<String>) null)))
                            : pfs.stream().map(pft -> t.concat(
                                    tuple(pft.v1, pft.v2)));
                })
                .collect(Collectors.toList());

        toCSV(big);
    }


    private static void toCSV(List<Tuple4<LogicalFlowKey, List<String>, PhysicalFlowKey, List<String>>> big) throws IOException {
        CsvListWriter writer = new CsvListWriter(new FileWriter("flows_with_comments.tsv"), CsvPreference.TAB_PREFERENCE);
        writer.writeHeader(
                "Logical Flow Id",
                "Source Name", "Source Ext Id", "Source Kind",
                "Target Name", "Target Ext Id", "Target Kind",
                "Logical DataTypes",
                "Physical Flow Id", "Physical Flow Name", "Physical Flow Ext Id", "Physical Flow Transport",
                "Specification Name", "Specification Ext Id",
                "Specification DataTypes",
                "Physical Flow Comment",
                "Specification Comment");

        big.forEach(unchecked(d -> writer.write(
                d.v1.lfId(),
                d.v1.src().name().orElse(""), d.v1.src().externalId().orElse(""), d.v1.src().kind(),
                d.v1.trg().name().orElse(""), d.v1.trg().externalId().orElse(""), d.v1.trg().kind(),
                toStr(d.v2),
                d.v3 == null ? null : toStr(d.v3.pfId()),
                d.v3 == null ? null : mkSafe(d.v3.flowName()),
                d.v3 == null ? null : mkSafe(d.v3.flowExtId()),
                d.v3 == null ? null : mkSafe(d.v3.transport()),
                d.v3 == null ? null : mkSafe(d.v3.specName()),
                d.v3 == null ? null : mkSafe(d.v3.specExtId()),
                toStr(d.v4),
                d.v3 == null ? null : mkSafe(d.v3.flowDescription()),
                d.v3 == null ? null : mkSafe(d.v3.specDescription()))));
    }


    private static String toStr(Long d) {
        return d == null ? "" : d.toString();
    }

    private static String toStr(List<String> d) {
        return d == null ? "" : d.stream().collect(Collectors.joining("; "));
    }

}
