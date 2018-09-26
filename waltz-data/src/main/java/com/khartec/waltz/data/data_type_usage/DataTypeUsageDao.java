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

package com.khartec.waltz.data.data_type_usage;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.model.data_type_usage.ImmutableDataTypeUsage;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.usage_info.ImmutableUsageInfo;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import com.khartec.waltz.schema.tables.records.DataTypeUsageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.limit;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.DataTypeUsage.DATA_TYPE_USAGE;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.jooq.impl.DSL.*;

@Repository
public class DataTypeUsageDao {

    private final com.khartec.waltz.schema.tables.DataType dt = DATA_TYPE.as("dt");
    private final com.khartec.waltz.schema.tables.DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");
    private final com.khartec.waltz.schema.tables.LogicalFlow lf = LOGICAL_FLOW.as("lf");
    private final com.khartec.waltz.schema.tables.LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");
    private final com.khartec.waltz.schema.tables.Application app = APPLICATION.as("app");
    private final com.khartec.waltz.schema.tables.Actor actor = ACTOR.as("actor");
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


    public List<Tally<String>> findUsageStatsForDataTypeSelector(Select<Record1<Long>> dataTypeIdSelector) {
        return dsl.select(DATA_TYPE_USAGE.USAGE_KIND, DSL.count())
                .from(DATA_TYPE_USAGE)
                .where(DATA_TYPE_USAGE.DATA_TYPE_ID.in(dataTypeIdSelector))
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
                .where(dsl.renderInlined(condition))
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

            // clear usages
            tx.deleteFrom(DATA_TYPE_USAGE)
                    .where(isCalculatedUsageKind)
                    .and(DATA_TYPE_USAGE.DESCRIPTION.eq(""))
                    .and(DATA_TYPE_USAGE.ENTITY_ID.in(idSelector))
                    .and(DATA_TYPE_USAGE.ENTITY_KIND.eq(kind.name()))
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
                        selectFrom(flowTable)
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
                .on(mkDataTypeUsageJoin(usageKindInner))
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

        com.khartec.waltz.schema.tables.DataTypeUsage dtuConsumer = DATA_TYPE_USAGE.as("dtu_consumer");

        return DSL.select(
                    dtu.ENTITY_ID.as(nodeIdInner),
                    dtu.DATA_TYPE_ID.as(dataTypeIdInner),
                    originatorUsageKindField.as(usageKindInner))
                .from(dtu)
                .where(dtu.ENTITY_ID.in(nodeIdSelector))
                .and(dtu.ENTITY_KIND.eq(nodeKind.name()))
                .and(dtu.USAGE_KIND.eq(UsageKind.DISTRIBUTOR.name()))
                .and(dtu.IS_SELECTED.eq(true))
                .and(DSL.notExists(DSL.selectFrom(dtuConsumer)
                        .where(dtuConsumer.ENTITY_ID.eq(dtu.ENTITY_ID))
                        .and(dtuConsumer.ENTITY_KIND.eq(dtu.ENTITY_KIND))
                        .and(dtuConsumer.DATA_TYPE_ID.eq(dtu.DATA_TYPE_ID))
                        .and(dtuConsumer.USAGE_KIND.eq(UsageKind.CONSUMER.name()))
                        .and(dtuConsumer.IS_SELECTED.eq(true))))
                .asTable("originators");

    }


    private Condition mkDataTypeUsageJoin(Field<String> usageKindField) {
        return dtu.ENTITY_ID.eq(nodeIdInner)
                        .and(dtu.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(dtu.DATA_TYPE_ID.eq(dataTypeIdInner))
                        .and(dtu.USAGE_KIND.eq(usageKindField))
                        .and(dtu.IS_SELECTED.eq(false));
    }


    public Map<Long, Collection<EntityReference>> findForUsageKindByDataTypeIdSelector(UsageKind kind,
                                                                                       Select<Record1<Long>> dataTypeIdSelector) {
        Result<Record3<Long, Long, String>> records = dsl
                .select(dt.ID, dtu.ENTITY_ID, app.NAME)
                .from(dtu)
                .innerJoin(app)
                .on(dtu.ENTITY_ID.eq(app.ID))
                .innerJoin(dt)
                .on(dt.ID.eq(dtu.DATA_TYPE_ID))
                .where(dtu.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(dt.ID.in(dataTypeIdSelector))
                .and(dtu.USAGE_KIND.eq(kind.name()))
                .fetch();

        return MapUtilities.groupBy(
                r -> r.value1(),
                r -> EntityReference.mkRef(EntityKind.APPLICATION, r.value2(), r.value3()),
                records);
    }

}
