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

package org.finos.waltz.data.datatype_decorator;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.datatype.DataTypeUsageCharacteristics;
import org.finos.waltz.model.datatype.ImmutableDataTypeDecorator;
import org.finos.waltz.model.datatype.ImmutableDataTypeUsageCharacteristics;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.jooq.lambda.function.Function2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static org.finos.waltz.model.EntityKind.DATA_TYPE;
import static org.finos.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.PhysicalSpecDataType.PHYSICAL_SPEC_DATA_TYPE;


@Repository
public class LogicalFlowDecoratorDao extends DataTypeDecoratorDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
            LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND,
            newArrayList(EntityKind.DATA_TYPE));


    private static final RecordMapper<Record, DataTypeDecorator> TO_DECORATOR_MAPPER = r -> {
        LogicalFlowDecoratorRecord record = r.into(LOGICAL_FLOW_DECORATOR);

        return ImmutableDataTypeDecorator.builder()
                .id(record.getId())
                .entityReference(mkRef(LOGICAL_DATA_FLOW, record.getLogicalFlowId()))
                .decoratorEntity(mkRef(
                        DATA_TYPE,
                        record.getDecoratorEntityId(),
                        r.get(ENTITY_NAME_FIELD)))
                .rating(AuthoritativenessRatingValue.ofNullable(record.getRating()))
                .provenance(record.getProvenance())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isReadonly(record.getIsReadonly())
                .flowClassificationRuleId(Optional.ofNullable(record.getFlowClassificationRuleId()))
                .build();
    };


    private static final Function<DataTypeDecorator, LogicalFlowDecoratorRecord> TO_RECORD = d -> {
        LogicalFlowDecoratorRecord r = new LogicalFlowDecoratorRecord();
        r.setId(d.id().orElse(null));
        r.changed(LOGICAL_FLOW_DECORATOR.ID, false);
        r.setDecoratorEntityKind(DATA_TYPE.name());
        r.setDecoratorEntityId(d.decoratorEntity().id());
        r.setLogicalFlowId(d.entityReference().id());
        r.setProvenance(d.provenance());
        d.rating().ifPresent(rating -> r.setRating(rating.value()));
        r.setLastUpdatedAt(Timestamp.valueOf(d.lastUpdatedAt()));
        r.setLastUpdatedBy(d.lastUpdatedBy());
        r.setIsReadonly(d.isReadonly());
        d.flowClassificationRuleId().ifPresent(r::setFlowClassificationRuleId);
        return r;
    };

    private final DSLContext dsl;

    @Autowired
    public LogicalFlowDecoratorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    // --- FINDERS ---
    @Override
    public DataTypeDecorator getByEntityIdAndDataTypeId(long flowId, long dataTypeId) {

        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(flowId))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name()))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(dataTypeId))
                .fetchOne(TO_DECORATOR_MAPPER);
    }

    @Override
    public List<DataTypeDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = LOGICAL_FLOW.TARGET_ENTITY_ID.in(appIdSelector)
                .or(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(appIdSelector));

        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .and(LOGICAL_NOT_REMOVED)
                .where(dsl.renderInlined(condition))
                .fetch(TO_DECORATOR_MAPPER);
    }

    @Override
    public List<DataTypeDecorator> findByDataTypeIdSelector(Select<Record1<Long>> decoratorEntityIdSelector) {
        checkNotNull(decoratorEntityIdSelector, "decoratorEntityIdSelector cannot be null");

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name())
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(decoratorEntityIdSelector));

        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .where(dsl.renderInlined(condition))
                .and(LOGICAL_NOT_REMOVED)
                .fetch(TO_DECORATOR_MAPPER);
    }


    /**
     * Deprecated - use findByLogicalFlowIdSelector instead
     */
    @Override
    @Deprecated
    public Set<DataTypeDecorator> findByFlowIds(Collection<Long> flowIds) {
        checkNotNull(flowIds, "flowIds cannot be null");

        Condition condition = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIds);

        return findByCondition(condition);
    }

    public List<DataTypeDecorator> findByFlowIdSelector(Select<Record1<Long>> flowIdSelector) {

        Condition condition = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIdSelector);

        return findByCondition(condition);
    }

    @Override
    public Set<DataTypeDecorator> findByLogicalFlowIdSelector(Select<Record1<Long>> flowIdSelector) {
        Condition condition = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.in(flowIdSelector);

        return findByCondition(condition);
    }


    public List<DataTypeDecorator> findAll() {
        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .fetch(TO_DECORATOR_MAPPER);
    }


    @Override
    public List<DataTypeDecorator> findByEntityId(long entityId) {
        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(entityId))
                .fetch(TO_DECORATOR_MAPPER);
    }

    @Override
    public List<DataTypeDecorator> findByEntityIdSelector(Select<Record1<Long>> entityIdSelector,
                                                          Optional<EntityKind> entityKind) {
            checkNotNull(entityKind, "entityKind cannot be null");
            checkNotNull(entityIdSelector, "entityIdSelector cannot be null");

            return dsl
                    .select(LOGICAL_FLOW_DECORATOR.fields())
                    .select(ENTITY_NAME_FIELD)
                    .from(LOGICAL_FLOW_DECORATOR)
                    .innerJoin(LOGICAL_FLOW)
                    .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                    .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.in(entityIdSelector)
                            .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(entityKind.get().name())))
                    .or(LOGICAL_FLOW.TARGET_ENTITY_ID.in(entityIdSelector)
                            .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(entityKind.get().name())))
                    .and(LOGICAL_NOT_REMOVED)
                    .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name()))
                    .fetch(TO_DECORATOR_MAPPER);
    }


    @Override
    public int removeDataTypes(EntityReference associatedEntityRef, Collection<Long> dataTypeIds) {
        return dsl
                .deleteFrom(LOGICAL_FLOW_DECORATOR)
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(associatedEntityRef.id()))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name()))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeIds))
                        .and(LOGICAL_FLOW_DECORATOR.IS_READONLY.isFalse())
                .execute();
    }


    @Override
    public int[] addDecorators(Collection<DataTypeDecorator> decorators) {
        checkNotNull(decorators, "decorators cannot be null");

        List<LogicalFlowDecoratorRecord> records = decorators
                .stream()
                .map(TO_RECORD)
                .collect(toList());

        Query[] queries = records.stream().map(
                record -> dsl
                        .insertInto(LOGICAL_FLOW_DECORATOR)
                        .set(record)
                        .onDuplicateKeyUpdate()
                        .set(record))
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
        // todo: in jOOQ 3.10.0 this can be written as follows #2979
        // return dsl.batchInsert(records).onDuplicateKeyIgnore().execute();
    }


    public int[] updateDecorators(Set<DataTypeDecorator> decorators) {
        Set<LogicalFlowDecoratorRecord> records = SetUtilities.map(decorators, TO_RECORD);
        return dsl.batchUpdate(records).execute();
    }


    public int updateDecoratorsForFlowClassificationRule(FlowClassificationRuleVantagePoint flowClassificationRuleVantagePoint) {
        LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");

        EntityReference vantagePoint = flowClassificationRuleVantagePoint.vantagePoint();
        EntityReference subjectReference = flowClassificationRuleVantagePoint.subjectReference();
        EntityReference dataType = flowClassificationRuleVantagePoint.dataType();
        String classificationCode = flowClassificationRuleVantagePoint.classificationCode();

        SelectConditionStep<Record1<Long>> orgUnitSubselect = DSL
                .select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(vantagePoint.kind().name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(vantagePoint.id()));

        SelectConditionStep<Record1<Long>> dataTypeSubselect = DSL
                .select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(DATA_TYPE.name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(dataType.id()));

        Condition usingFlowClassificationRule = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(subjectReference.id()).and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(subjectReference.kind().name()));
        Condition notUsingFlowClassificationRule = LOGICAL_FLOW.SOURCE_ENTITY_ID.ne(subjectReference.id()).or(LOGICAL_FLOW.SOURCE_ENTITY_KIND.ne(subjectReference.kind().name()));

        Function2<Condition, String, Update<LogicalFlowDecoratorRecord>> mkQuery = (subjectScopingCondition, ratingName) -> dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, ratingName)
                .set(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID, flowClassificationRuleVantagePoint.ruleId())
                .where(LOGICAL_FLOW_DECORATOR.ID.in(
                        DSL.select(lfd.ID)
                                .from(lfd)
                                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(lfd.LOGICAL_FLOW_ID))
                                .innerJoin(APPLICATION)
                                .on(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                                .where(subjectScopingCondition
                                        .and(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitSubselect))
                                        .and(lfd.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name()))
                                        .and(lfd.DECORATOR_ENTITY_ID.in(dataTypeSubselect))
                                        .and(lfd.RATING.in(
                                                AuthoritativenessRatingValue.NO_OPINION.value(),
                                                AuthoritativenessRatingValue.DISCOURAGED.value())))));

        Update<LogicalFlowDecoratorRecord> updateAuthSources = mkQuery.apply(
                usingFlowClassificationRule,
                classificationCode);

        Update<LogicalFlowDecoratorRecord> updateNonAuthSources = mkQuery.apply(
                notUsingFlowClassificationRule,
                AuthoritativenessRatingValue.DISCOURAGED.value());

        int authSourceUpdateCount = updateAuthSources.execute();
        int nonAuthSourceUpdateCount = updateNonAuthSources.execute();
        return authSourceUpdateCount + nonAuthSourceUpdateCount;
    }


    @Override
    public List<DataTypeUsageCharacteristics> findDatatypeUsageCharacteristics(EntityReference ref) {

        Field<Integer> numberOfFlowsSharingDatatype = DSL
                .countDistinct(PHYSICAL_FLOW.ID)
                .filterWhere(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.isNotNull())
                .as("numberOfFlowsSharingDatatype");

        return dsl
                .select(
                        LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID,
                        LOGICAL_FLOW_DECORATOR.IS_READONLY,
                        numberOfFlowsSharingDatatype)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID)
                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(DATA_TYPE.name())))
                .leftJoin(PHYSICAL_FLOW).on(PHYSICAL_FLOW.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID)
                        .and(PHYSICAL_FLOW.IS_REMOVED.isFalse())
                        .and(PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())))
                .leftJoin(PHYSICAL_SPEC_DATA_TYPE).on(PHYSICAL_FLOW.SPECIFICATION_ID.eq(PHYSICAL_SPEC_DATA_TYPE.SPECIFICATION_ID)
                        .and(PHYSICAL_SPEC_DATA_TYPE.DATA_TYPE_ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)))
                .where(LOGICAL_FLOW.ID.eq(ref.id()))
                .groupBy(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID, LOGICAL_FLOW_DECORATOR.IS_READONLY)
                .fetch(r -> {
                    int usageCount = r.get(numberOfFlowsSharingDatatype);
                    return ImmutableDataTypeUsageCharacteristics.builder()
                            .dataTypeId(r.get(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID))
                            .warningMessageForViewers(usageCount == 0
                                    ? "Warning: None of the underlying physical flows reference this data type."
                                    : null)
                            .warningMessageForEditors(usageCount > 0
                                    ? format("Cannot be removed as used in %d physical flows. These must be removed first.", usageCount)
                                    : null)
                            .isRemovable(usageCount == 0)
                            .build();
                });
    }


    public int updateRatingsByCondition(AuthoritativenessRatingValue rating, Condition condition) {
        return dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, rating.value())
                .where(condition)
                .execute();
    }


    // --- HELPERS ---

    private Set<DataTypeDecorator> findByCondition(Condition condition) {
        return dsl
                .select(LOGICAL_FLOW_DECORATOR.fields())
                .select(ENTITY_NAME_FIELD)
                .from(LOGICAL_FLOW_DECORATOR)
                .where(dsl.renderInlined(condition))
                .fetchSet(TO_DECORATOR_MAPPER);
    }

}
