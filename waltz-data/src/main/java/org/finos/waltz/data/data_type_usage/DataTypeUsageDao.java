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

package org.finos.waltz.data.data_type_usage;

import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.data_type_usage.DataTypeUsage;
import org.finos.waltz.model.data_type_usage.ImmutableDataTypeUsage;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.model.usage_info.ImmutableUsageInfo;
import org.finos.waltz.model.usage_info.UsageInfo;
import org.finos.waltz.model.usage_info.UsageKind;
import org.finos.waltz.schema.tables.records.DataTypeUsageRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Record7;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOrderByStep;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.limit;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.Actor.ACTOR;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;
import static org.finos.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.jooq.impl.DSL.*;

@Repository
public class DataTypeUsageDao {

    private final org.finos.waltz.schema.tables.DataType dt = DATA_TYPE.as("dt");
    private final org.finos.waltz.schema.tables.DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");
    private final org.finos.waltz.schema.tables.LogicalFlow lf = LOGICAL_FLOW.as("lf");
    private final org.finos.waltz.schema.tables.LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");
    private final org.finos.waltz.schema.tables.Application app = APPLICATION.as("app");
    private final org.finos.waltz.schema.tables.Actor actor = ACTOR.as("actor");
    private final Condition NOT_REMOVED = lf.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());

    private final Field<String> originatorUsageKindField = val(UsageKind.ORIGINATOR.name());



    private final Field<Long> nodeIdInner = DSL.field("node_id_inner", Long.class);
    private final Field<Long> dataTypeIdInner = DSL.field("dt_id_inner", Long.class);
    private final Field<String> usageKindInner = DSL.field("usage_kind_inner", String.class);

    private static final RecordMapper<Record, DataTypeUsage> TO_USAGE_MAPPER = r -> {
        DataTypeUsageRecord record = r.into(DATA_TYPE_USAGE);
        return ImmutableDataTypeUsage.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .build())
                .dataTypeId(record.getDataTypeId())
                .usage(ImmutableUsageInfo.builder()
                        .kind(UsageKind.valueOf(record.getUsageKind()))
                        .description(record.getDescription())
                        .isSelected(record.getIsSelected())
                        .build())
                .provenance(record.getProvenance())
                .build();
    };


    private static final Function<DataTypeUsage, DataTypeUsageRecord> TO_RECORD_MAPPER = domain -> {
        DataTypeUsageRecord record = new DataTypeUsageRecord();
        record.setEntityKind(domain.entityReference().kind().name());
        record.setEntityId(domain.entityReference().id());
        record.setDataTypeId(domain.dataTypeId());
        record.setProvenance(domain.provenance());
        record.setUsageKind(domain.usage().kind().name());
        record.setDescription(
                limit(
                    domain.usage().description(),
                    DATA_TYPE_USAGE.DESCRIPTION.getDataType().length()));
        record.setIsSelected(domain.usage().isSelected());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public DataTypeUsageDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<DataTypeUsage> findForIdSelector(EntityKind kind, Select<Record1<Long>> selector) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(selector, "selector cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.in(selector)));
    }


    public List<DataTypeUsage> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return findByCondition(
                DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name())
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id())));
    }


    public List<Tally<String>> findUsageStatsForDataTypeSelector(Select<Record1<Long>> dataTypeIdSelector,
                                                                 IdSelectionOptions options) {
        return dsl.select(DATA_TYPE_USAGE.USAGE_KIND, DSL.count())
                .from(DATA_TYPE_USAGE)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(DATA_TYPE_USAGE.ENTITY_ID)
                            .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(DATA_TYPE_USAGE.DATA_TYPE_ID.in(dataTypeIdSelector))
                .and(APPLICATION.KIND.notIn(options.filters().omitApplicationKinds()))
                .groupBy(DATA_TYPE_USAGE.USAGE_KIND)
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }


    public List<DataTypeUsage> findForDataTypeSelector(Select<Record1<Long>> dataTypeIdSelector) {
        checkNotNull(dataTypeIdSelector, "dataTypeIdSelector cannot be null");
        return findByCondition(DATA_TYPE_USAGE.DATA_TYPE_ID.in(dataTypeIdSelector));
    }


    public List<DataTypeUsage> findForEntityAndDataType(EntityReference ref, Long dataTypeId) {
        checkNotNull(ref, "ref cannot be null");

        return dsl.select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name())
                        .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                        .and(DATA_TYPE_USAGE.DATA_TYPE_ID.eq(dataTypeId)))
                .fetch(TO_USAGE_MAPPER);
    }


    private List<DataTypeUsage> findByCondition(Condition condition) {
        checkNotNull(condition, "condition cannot be null");
        return dsl
                .select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .where(condition)
                .fetch(TO_USAGE_MAPPER);
    }


    public int[] insertUsageInfo(EntityReference ref,
                                 Long dataTypeId,
                                 List<UsageInfo> inserts) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(inserts, "inserts cannot be null");

        Set<DataTypeUsageRecord> records = toRecords(ref, dataTypeId, inserts);

        return dsl
                .batchInsert(records)
                .execute();
    }


    public int deleteUsageInfo(EntityReference ref,
                               Long dataTypeId,
                               List<UsageKind> deletes) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(deletes, "deletes cannot be null");

        return dsl.deleteFrom(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(ref.id()))
                .and(DATA_TYPE_USAGE.DATA_TYPE_ID.eq(dataTypeId))
                .and(DATA_TYPE_USAGE.USAGE_KIND.in(deletes))
                .execute();
    }


    public int[] updateUsageInfo(EntityReference ref,
                                 Long dataTypeId,
                                 List<UsageInfo> updates) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(updates, "updates cannot be null");

        Set<DataTypeUsageRecord> records = toRecords(ref, dataTypeId, updates);

        return dsl
                .batchUpdate(records)
                .execute();
    }


    private Set<DataTypeUsageRecord> toRecords(EntityReference ref, Long dataTypeId, List<UsageInfo> usages) {
        ImmutableDataTypeUsage.Builder recordBuilder = ImmutableDataTypeUsage.builder()
                .provenance("waltz")
                .entityReference(ref)
                .dataTypeId(dataTypeId);

        return usages.stream()
                .map(i -> recordBuilder.usage(i).build())
                .map(TO_RECORD_MAPPER)
                .collect(Collectors.toSet());
    }


    public boolean recalculateForAllApplications() {
        recalculateForIdSelector(
                EntityKind.APPLICATION,
                DSL.select(APPLICATION.ID)
                    .from(APPLICATION)
                    .where(IS_ACTIVE));

        recalculateForIdSelector(
                EntityKind.ACTOR,
                DSL.select(ACTOR.ID)
                    .from(ACTOR));

        return true;
    }


    @Deprecated
    public boolean recalculateForAppIdSelector(Select<Record1<Long>> appIdSelector) {
        return recalculateForIdSelector(EntityKind.APPLICATION, appIdSelector);
    }


    public boolean recalculateForIdSelector(EntityKind kind, Select<Record1<Long>> idSelector) {

        dsl.transaction(configuration -> {
            DSLContext tx = DSL.using(configuration);

            Condition isCalculatedUsageKind = DATA_TYPE_USAGE.USAGE_KIND.in(
                    UsageKind.CONSUMER.name(),
                    UsageKind.ORIGINATOR.name(),
                    UsageKind.DISTRIBUTOR.name());

            // clear calculated usages
            tx.deleteFrom(DATA_TYPE_USAGE)
                    .where(isCalculatedUsageKind)
                    .and(DATA_TYPE_USAGE.DESCRIPTION.eq(""))
                    .and(DATA_TYPE_USAGE.ENTITY_ID.in(idSelector))
                    .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name()))
                    .execute();

            // clear usages where the datatype is not tied to an active logical flow
            List<DataTypeUsageRecord> recordsToDelete = findAllNonActiveDataTypeUsages(tx, kind, idSelector);
            tx.batchDelete(recordsToDelete)
                    .execute();



            // mark commented usages inactive
            tx.update(DATA_TYPE_USAGE)
                    .set(DATA_TYPE_USAGE.IS_SELECTED, false)
                    .where(isCalculatedUsageKind)
                    .and(DATA_TYPE_USAGE.DESCRIPTION.ne(""))
                    .and(DATA_TYPE_USAGE.ENTITY_ID.in(idSelector))
                    .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name()))
                    .execute();

            insertUsages(
                    tx,
                    mkConsumerDistributorUsagesToInsertSelector(kind, idSelector));
            updateUsageKinds(
                    tx,
                    mkFlowWithTypesForConsumerDistributors(kind, idSelector));

            insertUsages(
                    tx,
                    mkOriginatorUsagesToInsertSelector(kind, idSelector));
            updateUsageKinds(
                    tx,
                    mkFlowWithTypesForOriginators(kind, idSelector));
        });

        return true;

    }


    private List<DataTypeUsageRecord> findAllNonActiveDataTypeUsages(DSLContext tx,
                                                                     EntityKind kind,
                                                                     Select<Record1<Long>> idSelector) {
        Field<String> entityKind = field("entity_kind", String.class);
        Field<Long> entityId = field("entity_id", Long.class);
        Field<Long> dataTypeId = field("data_type_id", Long.class);

        SelectConditionStep<Record3<String, Long, Long>> logicalSourcesWithDataTypes = tx.select(
                LOGICAL_FLOW.SOURCE_ENTITY_KIND.as(entityKind),
                LOGICAL_FLOW.SOURCE_ENTITY_ID.as(entityId),
                LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.as(dataTypeId)
        )
        .from(LOGICAL_FLOW_DECORATOR)
        .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
        .where(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
        .and(LOGICAL_FLOW.IS_REMOVED.isFalse().and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())))
        .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(idSelector))
        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(kind.name()));


        SelectConditionStep<Record3<String, Long, Long>> logicalTargetsWithDataTypes = tx.select(
                LOGICAL_FLOW.TARGET_ENTITY_KIND.as(entityKind),
                LOGICAL_FLOW.TARGET_ENTITY_ID.as(entityId),
                LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.as(dataTypeId)
        )
        .from(LOGICAL_FLOW_DECORATOR)
        .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
        .where(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
        .and(LOGICAL_FLOW.IS_REMOVED.isFalse().and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())))
        .and(LOGICAL_FLOW.TARGET_ENTITY_ID.in(idSelector))
        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(kind.name()));

        SelectOrderByStep<Record3<String, Long, Long>> refsWithActiveFlowDataTypes = logicalSourcesWithDataTypes
                .union(logicalTargetsWithDataTypes);

        SelectConditionStep<Record> dataTypeUsagesThatNeedDeleting = tx
                .select(DATA_TYPE_USAGE.fields())
                .from(DATA_TYPE_USAGE)
                .leftOuterJoin(refsWithActiveFlowDataTypes)
                .on(DATA_TYPE_USAGE.ENTITY_KIND.eq(refsWithActiveFlowDataTypes.field(entityKind)))
                .and(DATA_TYPE_USAGE.ENTITY_ID.eq(refsWithActiveFlowDataTypes.field(entityId)))
                .and(DATA_TYPE_USAGE.DATA_TYPE_ID.eq(refsWithActiveFlowDataTypes.field(dataTypeId)))
                .where(refsWithActiveFlowDataTypes.field(entityId).isNull())
                .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name()))
                .and(DATA_TYPE_USAGE.ENTITY_ID.in(idSelector));

        return dataTypeUsagesThatNeedDeleting
                .fetch()
                .map(r -> r.into(DATA_TYPE_USAGE));
    }



    private void insertUsages(DSLContext tx,
                              Select<Record7<Long, String, Long, String, String, String, Boolean>> usagesSelector) {
        tx.insertInto(DATA_TYPE_USAGE)
                .columns(
                        DATA_TYPE_USAGE.ENTITY_ID,
                        DATA_TYPE_USAGE.ENTITY_KIND,
                        DATA_TYPE_USAGE.DATA_TYPE_ID,
                        DATA_TYPE_USAGE.USAGE_KIND,
                        DATA_TYPE_USAGE.DESCRIPTION,
                        DATA_TYPE_USAGE.PROVENANCE,
                        DATA_TYPE_USAGE.IS_SELECTED)
                .select(usagesSelector)
                .execute();
    }


    private void updateUsageKinds(DSLContext tx,
                                  Table<Record3<Long, Long, String>> flowTable) {
        tx.update(DATA_TYPE_USAGE)
                .set(DATA_TYPE_USAGE.IS_SELECTED, true)
                .where(DATA_TYPE_USAGE.IS_SELECTED.eq(false))
                .and(exists(
                        select(flowTable.fields())
                                .from(flowTable)
                                .where(DATA_TYPE_USAGE.ENTITY_ID.eq(nodeIdInner))
                                .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                                .and(DATA_TYPE_USAGE.DATA_TYPE_ID.eq(dataTypeIdInner))
                                .and(DATA_TYPE_USAGE.USAGE_KIND.eq(usageKindInner))))
                .execute();
    }


    private Select<Record7<Long, String, Long, String, String, String, Boolean>> mkOriginatorUsagesToInsertSelector(
            EntityKind nodeKind,
            Select<Record1<Long>> nodeIdSelector)
    {
        return mkUsagesToInsertSelector(
                nodeKind,
                mkFlowWithTypesForOriginators(
                        nodeKind,
                        nodeIdSelector));
    }

    private Select<Record7<Long, String, Long, String, String, String, Boolean>> mkConsumerDistributorUsagesToInsertSelector(
            EntityKind kind,
            Select<Record1<Long>> idSelector)
    {
        return mkUsagesToInsertSelector(
                kind,
                mkFlowWithTypesForConsumerDistributors(
                        kind,
                        idSelector));
    }


    private Select<Record7<Long, String, Long, String, String, String, Boolean>> mkUsagesToInsertSelector(
            EntityKind nodeKind,
            Table<Record3<Long, Long, String>> flowsWithTypesTable)
    {
        return selectDistinct(
                nodeIdInner,
                val(nodeKind.name()),
                dataTypeIdInner,
                usageKindInner,
                val(""),
                val("waltz"),
                val(true))
            .from(flowsWithTypesTable)
            .leftJoin(dtu)
                .on(mkDataTypeUsageJoin(nodeKind, usageKindInner))
            .where(dtu.ENTITY_ID.isNull());
    }


    private Table<Record3<Long, Long, String>> mkFlowWithTypesForConsumerDistributors(
            EntityKind nodeKind,
            Select<Record1<Long>> nodeIdSelector)
    {

        switch (nodeKind) {
            case ACTOR:
                return mkFlowWithTypesForConsumerDistributors(nodeKind, actor, actor.ID, nodeIdSelector);
            case APPLICATION:
                return mkFlowWithTypesForConsumerDistributors(nodeKind, app, app.ID, nodeIdSelector);
            default:
                throw new UnsupportedOperationException("Cannot create dt usage records for node kind: "+nodeKind);
        }
    }


    private Table<Record3<Long, Long, String>> mkFlowWithTypesForConsumerDistributors(EntityKind nodeKind,
                                                                                        Table nodeTable,
                                                                                        TableField<? extends Record, Long> nodeIdField,
                                                                                        Select<Record1<Long>> nodeIdSelector) {
        return DSL
                .select(
                    nodeIdField.as(nodeIdInner),
                    dt.ID.as(dataTypeIdInner),
                    mkConsumerDistributorCaseField(nodeIdField).as(usageKindInner))
                .from(lf)
                .innerJoin(nodeTable)
                .on(mkMatchingFlowCondition(nodeKind, nodeIdField))
                .innerJoin(lfd)
                .on(lfd.LOGICAL_FLOW_ID.eq(lf.ID))
                .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .innerJoin(dt)
                .on(dt.ID.eq(lfd.DECORATOR_ENTITY_ID))
                .where(nodeIdField.in(nodeIdSelector))
                .and(NOT_REMOVED)
                .asTable("cons_dist");
    }


    private Condition mkMatchingFlowCondition(EntityKind nodeKind, TableField<? extends Record, Long> nodeIdField) {
        Condition matchingSourceCondition = nodeIdField.eq(lf.SOURCE_ENTITY_ID).and(lf.SOURCE_ENTITY_KIND.eq(nodeKind.name()));
        Condition matchingTargetCondition = nodeIdField.eq(lf.TARGET_ENTITY_ID).and(lf.TARGET_ENTITY_KIND.eq(nodeKind.name()));
        return matchingSourceCondition.or(matchingTargetCondition);
    }


    private Field<String> mkConsumerDistributorCaseField(TableField<? extends Record, Long> nodeIdTableField) {
        return DSL
                .when(lf.SOURCE_ENTITY_ID.eq(nodeIdTableField),
                        UsageKind.DISTRIBUTOR.name())
                .otherwise(UsageKind.CONSUMER.name());
    }


    private Table<Record3<Long, Long, String>> mkFlowWithTypesForOriginators(EntityKind nodeKind,
                                                                               Select<Record1<Long>> nodeIdSelector) {

        org.finos.waltz.schema.tables.DataTypeUsage dtuConsumer = DATA_TYPE_USAGE.as("dtu_consumer");

        return DSL.select(
                    dtu.ENTITY_ID.as(nodeIdInner),
                    dtu.DATA_TYPE_ID.as(dataTypeIdInner),
                    originatorUsageKindField.as(usageKindInner))
                .from(dtu)
                .where(dtu.ENTITY_ID.in(nodeIdSelector))
                .and(dtu.ENTITY_KIND.eq(nodeKind.name()))
                .and(dtu.USAGE_KIND.eq(UsageKind.DISTRIBUTOR.name()))
                .and(dtu.IS_SELECTED.eq(true))
                .and(DSL.notExists(DSL.select(dtuConsumer.fields())
                        .from(dtuConsumer)
                        .where(dtuConsumer.ENTITY_ID.eq(dtu.ENTITY_ID))
                        .and(dtuConsumer.ENTITY_KIND.eq(dtu.ENTITY_KIND))
                        .and(dtuConsumer.DATA_TYPE_ID.eq(dtu.DATA_TYPE_ID))
                        .and(dtuConsumer.USAGE_KIND.eq(UsageKind.CONSUMER.name()))
                        .and(dtuConsumer.IS_SELECTED.eq(true))))
                .asTable("originators");

    }


    private Condition mkDataTypeUsageJoin(EntityKind nodeKind, Field<String> usageKindField) {
        return dtu.ENTITY_ID.eq(nodeIdInner)
                        .and(dtu.ENTITY_KIND.eq(nodeKind.name()))
                        .and(dtu.DATA_TYPE_ID.eq(dataTypeIdInner))
                        .and(dtu.USAGE_KIND.eq(usageKindField))
                        .and(dtu.IS_SELECTED.eq(false));
    }


    public Map<Long, Collection<EntityReference>> findForUsageKindByDataTypeIdSelector(UsageKind kind,
                                                                                       Select<Record1<Long>> dataTypeIdSelector,
                                                                                       IdSelectionOptions options) {
        Result<Record3<Long, Long, String>> records = dsl
                .select(dt.ID, dtu.ENTITY_ID, app.NAME)
                .from(dtu)
                .innerJoin(app).on(dtu.ENTITY_ID.eq(app.ID))
                .innerJoin(dt).on(dt.ID.eq(dtu.DATA_TYPE_ID))
                .where(dtu.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(dt.ID.in(dataTypeIdSelector))
                .and(dtu.USAGE_KIND.eq(kind.name()))
                .and(app.KIND.notIn(options.filters().omitApplicationKinds()))
                .fetch();

        return MapUtilities.groupBy(
                Record3::value1,
                r -> mkRef(EntityKind.APPLICATION, r.value2(), r.value3()),
                records);
    }

}
