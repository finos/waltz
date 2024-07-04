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

package org.finos.waltz.data.flow_classification_rule;

import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.MessageSeverity;
import org.finos.waltz.model.datatype.FlowDataType;
import org.finos.waltz.model.flow_classification_rule.DiscouragedSource;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleCreateCommand;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleUpdateCommand;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.model.flow_classification_rule.ImmutableDiscouragedSource;
import org.finos.waltz.model.flow_classification_rule.ImmutableFlowClassificationRule;
import org.finos.waltz.model.flow_classification_rule.ImmutableFlowClassificationRuleVantagePoint;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.records.FlowClassificationRuleRecord;
import org.finos.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.SelectSeekStep2;
import org.jooq.SelectSeekStep6;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.FLOW_CLASSIFICATION;
import static org.finos.waltz.schema.Tables.FLOW_CLASSIFICATION_RULE;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class FlowClassificationRuleDao {

    public final static Application CONSUMER_APP = APPLICATION.as("consumer");
    public final static Application SUPPLIER_APP = APPLICATION.as("supplier");
    public final static Application SUBJECT_APP = APPLICATION.as("subject_app");
    public final static Application SUBJECT_EUDA = APPLICATION.as("subject_euda");
    public static final org.finos.waltz.schema.tables.DataType parent_dt = org.finos.waltz.schema.tables.DataType.DATA_TYPE.as("parent_dt");
    public static final org.finos.waltz.schema.tables.DataType child_dt = org.finos.waltz.schema.tables.DataType.DATA_TYPE.as("child_dt");
    public static final EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
    public static final EntityHierarchy level = ENTITY_HIERARCHY.as("level");

    private final static AggregateFunction<Integer> COUNT_FIELD = DSL.count(LOGICAL_FLOW);

    private final static EntityHierarchy ehOrgUnit = ENTITY_HIERARCHY.as("ehOrgUnit");
    private final static EntityHierarchy ehDataType = ENTITY_HIERARCHY.as("ehDataType");
    public static final Field<Long> vantagePointId = DSL.coalesce(ehOrgUnit.ID, FLOW_CLASSIFICATION_RULE.PARENT_ID);
    public static final Field<Integer> vantagePointLevel = DSL.coalesce(ehOrgUnit.LEVEL, 0).as("parentLevel");
    public static final Field<Integer> dataTypeLevel = DSL.coalesce(ehDataType.LEVEL, 0).as("dataTypeLevel");
    private static final Field<String> PARENT_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            FLOW_CLASSIFICATION_RULE.PARENT_ID,
            FLOW_CLASSIFICATION_RULE.PARENT_KIND,
            newArrayList(EntityKind.ORG_UNIT, EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Field<String> SUBJECT_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID,
            FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR, EntityKind.END_USER_APPLICATION));

    private static final Condition flowNotRemoved = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())
            .and(LOGICAL_FLOW.IS_REMOVED.isFalse());
    private static final Condition supplierNotRemoved = SUPPLIER_APP.IS_REMOVED.isFalse();
    private static final Condition consumerNotRemoved = CONSUMER_APP.IS_REMOVED.isFalse();

    private final DSLContext dsl;

    private static final RecordMapper<Record, FlowClassificationRule> TO_DOMAIN_MAPPER = r -> {
        FlowClassificationRuleRecord record = r.into(FlowClassificationRuleRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .name(r.get(PARENT_NAME_FIELD))
                .build();

        //Actors now supported, these do not have owning org units
        EntityReference orgUnitRef = Optional.ofNullable(r.getValue(ORGANISATIONAL_UNIT.ID))
                .map(orgId -> ImmutableEntityReference.builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(orgId)
                        .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                        .build())
                .orElse(null);

        EntityReference subjectRef = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(r.getValue(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND)))
                .id(r.getValue(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID))
                .name(r.getValue(SUBJECT_NAME_FIELD))
                .build();

        return ImmutableFlowClassificationRule.builder()
                .id(record.getId())
                .vantagePointReference(parentRef)
                .subjectOrgUnitReference(orgUnitRef)
                .subjectReference(subjectRef)
                .dataTypeId(record.getDataTypeId())
                .classificationId(record.getFlowClassificationId())
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .isReadonly(record.getIsReadonly())
                .message(record.getMessage())
                .messageSeverity(MessageSeverity.valueOf(record.getMessageSeverity()))
                .build();
    };


    private static final RecordMapper<Record, FlowClassificationRuleVantagePoint> TO_VANTAGE_MAPPER = r -> {
        return ImmutableFlowClassificationRuleVantagePoint
                .builder()
                .vantagePoint(mkRef(EntityKind.valueOf(r.get(FLOW_CLASSIFICATION_RULE.PARENT_KIND)), r.get(vantagePointId))) // could be child org unit
                .vantagePointRank(r.get(vantagePointLevel))
                .subjectReference(mkRef(EntityKind.valueOf(r.get(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND)), r.get(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)))
                .classificationCode(r.get(FLOW_CLASSIFICATION.CODE))
                .dataTypeId(r.get(ehDataType.ID))
                .dataTypeRank(r.get(dataTypeLevel))
                .ruleId(r.get(FLOW_CLASSIFICATION_RULE.ID))
                .message(r.get(FLOW_CLASSIFICATION_RULE.MESSAGE))
                .messageSeverity(MessageSeverity.valueOf(r.get(FLOW_CLASSIFICATION_RULE.MESSAGE_SEVERITY)))
                .build();
    };



    @Autowired
    public FlowClassificationRuleDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<FlowClassificationRule> findAll() {
        return baseSelect()
                .fetch(TO_DOMAIN_MAPPER);
    }


    public FlowClassificationRule getById(long id) {
        return baseSelect()
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<FlowClassificationRule> findByEntityKind(EntityKind kind) {
        checkNotNull(kind, "kind must not be null");
        
        return baseSelect()
                .where(FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(kind.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<FlowClassificationRule> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref must not be null");

        return baseSelect()
                .where(FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(ref.kind().name()))
                .and(FLOW_CLASSIFICATION_RULE.PARENT_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<FlowClassificationRule> findByApplicationId(long applicationId) {
        checkTrue(applicationId > -1, "applicationId must be +ve");
        
        return baseSelect()
                .where(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.eq(applicationId)
                        .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int update(FlowClassificationRuleUpdateCommand command) {
        checkNotNull(command, "command cannot be null");
        checkTrue(command.id().isPresent(), "id must be +ve");

        UpdateSetMoreStep<FlowClassificationRuleRecord> upd = dsl
                .update(FLOW_CLASSIFICATION_RULE)
                .set(FLOW_CLASSIFICATION_RULE.FLOW_CLASSIFICATION_ID, command.classificationId())
                .set(FLOW_CLASSIFICATION_RULE.DESCRIPTION, command.description())
                .set(FLOW_CLASSIFICATION_RULE.DIRECTION, DSL
                        .select(FLOW_CLASSIFICATION.DIRECTION)
                        .from(FLOW_CLASSIFICATION)
                        .where(FLOW_CLASSIFICATION.ID.eq(command.classificationId())));

        if (command.severity() != null) {
            upd.set(FLOW_CLASSIFICATION_RULE.MESSAGE_SEVERITY, command.severity().name());
            upd.set(FLOW_CLASSIFICATION_RULE.MESSAGE, command.message());
        }

        return upd
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(command.id().get()))
                .execute();
    }


    public long insert(FlowClassificationRuleCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        InsertSetMoreStep<FlowClassificationRuleRecord> stmt = dsl
                .insertInto(FLOW_CLASSIFICATION_RULE)
                .set(FLOW_CLASSIFICATION_RULE.PARENT_KIND, command.parentReference().kind().name())
                .set(FLOW_CLASSIFICATION_RULE.PARENT_ID, command.parentReference().id())
                .set(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID, command.dataTypeId())
                .set(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND, command.subjectReference().kind().name())
                .set(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID, command.subjectReference().id())
                .set(FLOW_CLASSIFICATION_RULE.FLOW_CLASSIFICATION_ID, command.classificationId())
                .set(FLOW_CLASSIFICATION_RULE.DESCRIPTION, command.description())
                .set(FLOW_CLASSIFICATION_RULE.PROVENANCE, "waltz")
                .set(FLOW_CLASSIFICATION_RULE.LAST_UPDATED_AT, nowUtcTimestamp())
                .set(FLOW_CLASSIFICATION_RULE.LAST_UPDATED_BY, username)
                .set(FLOW_CLASSIFICATION_RULE.DIRECTION, DSL
                        .select(FLOW_CLASSIFICATION.DIRECTION)
                        .from(FLOW_CLASSIFICATION)
                        .where(FLOW_CLASSIFICATION.ID.eq(command.classificationId())));

        if (command.severity() != null) {
            stmt.set(FLOW_CLASSIFICATION_RULE.MESSAGE_SEVERITY, command.severity().name());
            stmt.set(FLOW_CLASSIFICATION_RULE.MESSAGE, command.message());
        }

        return stmt
                .returning(FLOW_CLASSIFICATION_RULE.ID)
                .fetchOne()
                .getId();
    }


    public int remove(long id) {
        return dsl
                .delete(FLOW_CLASSIFICATION_RULE)
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(id))
                .execute();
    }


    public Set<EntityReference> cleanupOrphans() {
        Select<Record1<Long>> orgUnitIds = DSL
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT);

        Select<Record1<Long>> appIds = DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(IS_ACTIVE);

        Condition unknownOrgUnit = FLOW_CLASSIFICATION_RULE.PARENT_ID.notIn(orgUnitIds)
                .and(FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name())
                        .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

        Condition appIsInactive = FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.notIn(appIds)
                .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Set<EntityReference> appsInRulesWithoutOrgUnit = dsl
                .select(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)
                .from(FLOW_CLASSIFICATION_RULE)
                .where(unknownOrgUnit)
                .fetchSet(r -> mkRef(EntityKind.APPLICATION, r.get(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)));

        Set<EntityReference> orgUnitsInRulesWithoutApp = dsl
                .select(FLOW_CLASSIFICATION_RULE.PARENT_ID, FLOW_CLASSIFICATION_RULE.PARENT_KIND)
                .from(FLOW_CLASSIFICATION_RULE)
                .where(appIsInactive)
                .fetchSet(r -> mkRef(
                        EntityKind.valueOf(r.get(FLOW_CLASSIFICATION_RULE.PARENT_KIND)),
                        r.get(FLOW_CLASSIFICATION_RULE.PARENT_ID)));

        Set<EntityReference> bereaved = union(appsInRulesWithoutOrgUnit, orgUnitsInRulesWithoutApp);

        int deleted = dsl
                .deleteFrom(FLOW_CLASSIFICATION_RULE)
                .where(unknownOrgUnit)
                .or(appIsInactive)
                .execute();

        return bereaved;
    }


    /* deprecating as we need to work on improving the speed of on-demand recalcs */
    @Deprecated
    public int clearRatingsForPointToPointFlows(FlowClassificationRule rule) {

        // this may wipe any lower level explicit datatype mappings but these will be restored by the nightly job
        SelectConditionStep<Record1<Long>> decoratorsToMarkAsNoOpinion = dsl
                .select(LOGICAL_FLOW_DECORATOR.ID)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .innerJoin(ENTITY_HIERARCHY).on(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(ENTITY_HIERARCHY.ID))
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.DATA_TYPE.name()))
                .innerJoin(org.finos.waltz.schema.tables.DataType.DATA_TYPE).on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(org.finos.waltz.schema.tables.DataType.DATA_TYPE.ID))
                .innerJoin(FLOW_CLASSIFICATION).on(LOGICAL_FLOW_DECORATOR.RATING.eq(FLOW_CLASSIFICATION.CODE))
                .where(dsl.renderInlined(org.finos.waltz.schema.tables.DataType.DATA_TYPE.ID.eq(rule.dataTypeId())
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(rule.subjectReference().id())
                                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(rule.vantagePointReference().kind().name())
                                                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(rule.vantagePointReference().id()))))
                                .and(FLOW_CLASSIFICATION.ID.eq(rule.classificationId())))));

        return dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, AuthoritativenessRatingValue.NO_OPINION.value())
                .setNull(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID)
                .where(LOGICAL_FLOW_DECORATOR.ID.in(decoratorsToMarkAsNoOpinion))
                .execute();
    }


    public List<FlowClassificationRuleVantagePoint> findExpandedFlowClassificationRuleVantagePoints(FlowDirection direction,
                                                                                                    Set<Long> orgVantagePointIds,
                                                                                                    Set<Long> appVantagePointIds,
                                                                                                    Set<Long> actorVantagePointIds) {

        Condition appVantagePointCondition = FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.APPLICATION.name())
                .and(FLOW_CLASSIFICATION_RULE.PARENT_ID.in(appVantagePointIds));

        Condition actorVantagePointCondition = FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.ACTOR.name())
                .and(FLOW_CLASSIFICATION_RULE.PARENT_ID.in(actorVantagePointIds));

        Condition orgUnitVantagePointCondition = FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name())
                .and(ehOrgUnit.ID.in(orgVantagePointIds));

        Condition vantagePointCondition = appVantagePointCondition
                .or(actorVantagePointCondition)
                .or(orgUnitVantagePointCondition);

        SelectConditionStep<Record11<Long, String, Integer, Long, Integer, Long, String, String, Long, String, String>> select = dsl
                .select(vantagePointId,
                        FLOW_CLASSIFICATION_RULE.PARENT_KIND,
                        vantagePointLevel,
                        ehDataType.ID,
                        dataTypeLevel,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND,
                        FLOW_CLASSIFICATION.CODE,
                        FLOW_CLASSIFICATION_RULE.ID,
                        FLOW_CLASSIFICATION_RULE.MESSAGE,
                        FLOW_CLASSIFICATION_RULE.MESSAGE_SEVERITY)
                .from(FLOW_CLASSIFICATION_RULE)
                .innerJoin(FLOW_CLASSIFICATION).on(FLOW_CLASSIFICATION.DIRECTION.eq(direction.name())
                        .and(FLOW_CLASSIFICATION_RULE.FLOW_CLASSIFICATION_ID.eq(FLOW_CLASSIFICATION.ID)))
                .leftJoin(ehDataType)
                    .on(ehDataType.KIND.eq(EntityKind.DATA_TYPE.name())
                            .and(ehDataType.ANCESTOR_ID.eq(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID)))
                .leftJoin(ehOrgUnit)
                .on(FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name())
                        .and(ehOrgUnit.KIND.eq(EntityKind.ORG_UNIT.name())
                                .and(ehOrgUnit.ANCESTOR_ID.eq(FLOW_CLASSIFICATION_RULE.PARENT_ID)
                                        .and(ehOrgUnit.ID.in(orgVantagePointIds)))))
                .where(vantagePointCondition);

        return select.fetch(TO_VANTAGE_MAPPER);
    }


    public List<FlowClassificationRuleVantagePoint> findFlowClassificationRuleVantagePoints(FlowDirection direction) {
        return findFlowClassificationRuleVantagePoints(FLOW_CLASSIFICATION.DIRECTION.eq(direction.name()));
    }


    public List<FlowClassificationRuleVantagePoint> findFlowClassificationRuleVantagePoints(FlowDirection direction,
                                                                                            Set<Long> dataTypeIdsToConsider) {
        return findFlowClassificationRuleVantagePoints(
                FLOW_CLASSIFICATION.DIRECTION.eq(direction.name())
                        .and(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.in(dataTypeIdsToConsider).or(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.isNull())));
    }



    public List<FlowClassificationRuleVantagePoint> findFlowClassificationRuleVantagePoints(FlowDirection direction,
                                                                                            org.finos.waltz.model.entity_hierarchy.EntityHierarchy dtHierarchy,
                                                                                            Set<FlowDataType> population) {
        Set<Long> possibleDtIds = population
                .stream()
                .map(FlowDataType::dtId)
                .distinct()
                .flatMap(dtId -> dtHierarchy.findAncestors(dtId).stream())
                .collect(Collectors.toSet());
        return findFlowClassificationRuleVantagePoints(direction, possibleDtIds);
    }


    private List<FlowClassificationRuleVantagePoint> findFlowClassificationRuleVantagePoints(Condition condition) {
        SelectSeekStep6<Record11<Long, String, Integer, Long, Integer, Long, String, String, Long, String, String>, String, Integer, Integer, Long, Long, Long> select = dsl
                .select(vantagePointId,
                        FLOW_CLASSIFICATION_RULE.PARENT_KIND,
                        vantagePointLevel,
                        ehDataType.ID,
                        dataTypeLevel,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND,
                        FLOW_CLASSIFICATION.CODE,
                        FLOW_CLASSIFICATION_RULE.ID,
                        FLOW_CLASSIFICATION_RULE.MESSAGE,
                        FLOW_CLASSIFICATION_RULE.MESSAGE_SEVERITY)
                .from(FLOW_CLASSIFICATION_RULE)
                .leftJoin(ehOrgUnit)
                .on(ehOrgUnit.ANCESTOR_ID.eq(FLOW_CLASSIFICATION_RULE.PARENT_ID)
                        .and(ehOrgUnit.KIND.eq(EntityKind.ORG_UNIT.name()))
                        .and(ehOrgUnit.ID.eq(ehOrgUnit.ANCESTOR_ID)))
                .leftJoin(ehDataType)
                .on(ehDataType.ANCESTOR_ID.eq(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID)
                        .and(ehDataType.KIND.eq(EntityKind.DATA_TYPE.name()))
                        .and(ehDataType.ID.eq(ehDataType.ANCESTOR_ID)))
                .innerJoin(FLOW_CLASSIFICATION).on(FLOW_CLASSIFICATION_RULE.FLOW_CLASSIFICATION_ID.eq(FLOW_CLASSIFICATION.ID))
                .where(condition)
                .orderBy(
                        FLOW_CLASSIFICATION_RULE.PARENT_KIND, //ACTOR, APPLICATION, ORG_UNIT
                        vantagePointLevel.desc(),
                        dataTypeLevel.desc(),
                        vantagePointId,
                        ehDataType.ID,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID
                );

        return select.fetch(TO_VANTAGE_MAPPER);
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            Select<Record1<Long>> dataTypeIdSelector) {

        Condition appJoin = APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                .and(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ENTITY_HIERARCHY.ID));

        Condition hierarchyJoin = ENTITY_HIERARCHY.ANCESTOR_ID.eq(FLOW_CLASSIFICATION_RULE.PARENT_ID)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition flowClassificationRuleJoin = FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND));

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeIdSelector)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.in(dataTypeIdSelector))
                .and(flowNotRemoved);

        Field<Long> classificationRuleIdField = FLOW_CLASSIFICATION_RULE.ID.as("classification_rule_id");
        Field<Long> applicationIdField = APPLICATION.ID.as("application_id");
        Field<String> applicationNameField = APPLICATION.NAME.as("application_name");

        SelectSeekStep2<Record3<Long, Long, String>, Long, String> qry = dsl
                .select(classificationRuleIdField,
                        applicationIdField,
                        applicationNameField)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .innerJoin(FLOW_CLASSIFICATION_RULE).on(flowClassificationRuleJoin)
                .innerJoin(ENTITY_HIERARCHY).on(hierarchyJoin)
                .innerJoin(APPLICATION).on(appJoin)
                .where(condition)
                .orderBy(FLOW_CLASSIFICATION_RULE.ID, APPLICATION.NAME);

        Result<Record3<Long, Long, String>> records = qry
                .fetch();

        return groupBy(
                r -> mkRef(
                        EntityKind.FLOW_CLASSIFICATION_RULE,
                        r.getValue(classificationRuleIdField)),
                r -> mkRef(
                        EntityKind.APPLICATION,
                        r.getValue(applicationIdField),
                        r.getValue(applicationNameField)),
                records);
    }


    public List<DiscouragedSource> findDiscouragedSourcesBySelector(Condition customSelectionCriteria) {

        Condition decorationIsAboutDataTypes = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name());

        Condition badFlow = LOGICAL_FLOW_DECORATOR.RATING.in(
                AuthoritativenessRatingValue.DISCOURAGED.value(),
                AuthoritativenessRatingValue.NO_OPINION.value());

        Condition commonSelectionCriteria = flowNotRemoved
                .and(consumerNotRemoved)
                .and(supplierNotRemoved)
                .and(decorationIsAboutDataTypes)
                .and(badFlow);

        return dsl
                .select(SUPPLIER_APP.ID, SUPPLIER_APP.NAME)
                .select(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                .select(DSL.count(LOGICAL_FLOW))
                .from(SUPPLIER_APP)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(SUPPLIER_APP.ID)
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .innerJoin(LOGICAL_FLOW_DECORATOR)
                .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .innerJoin(CONSUMER_APP)
                .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(CONSUMER_APP.ID)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(customSelectionCriteria).and(commonSelectionCriteria)
                .groupBy(SUPPLIER_APP.ID, SUPPLIER_APP.NAME, LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                .fetch()
                .map(r -> ImmutableDiscouragedSource.builder()
                            .sourceReference(mkRef(
                                    EntityKind.APPLICATION,
                                    r.get(SUPPLIER_APP.ID),
                                    r.get(SUPPLIER_APP.NAME)))
                            .dataTypeId(r.get(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID))
                            .count(r.get(COUNT_FIELD))
                            .build());
    }


    public Set<FlowClassificationRule> findClassificationRules(Condition customSelectionCriteria) {

        SelectConditionStep<Record> qry = baseSelect()
                .where(customSelectionCriteria);

        return qry
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    // -- HELPERS --

    private SelectOnConditionStep<Record> baseSelect() {
        return dsl
                .select(PARENT_NAME_FIELD)
                .select(SUBJECT_NAME_FIELD)
                .select(ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME)
                .select(FLOW_CLASSIFICATION_RULE.fields())
                .from(FLOW_CLASSIFICATION_RULE)
                .leftJoin(SUBJECT_APP)
                .on(SUBJECT_APP.ID.eq(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)
                        .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(SUBJECT_EUDA)
                .on(SUBJECT_EUDA.ID.eq(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)
                        .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name())))
                .leftJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(SUBJECT_APP.ORGANISATIONAL_UNIT_ID)
                        .or(ORGANISATIONAL_UNIT.ID.eq(SUBJECT_EUDA.ORGANISATIONAL_UNIT_ID)));
    }


    public int updatePointToPointFlowClassificationRules(FlowDirection direction) {

        Condition logicalFlowTargetIsAuthSourceParent = FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND)
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(FLOW_CLASSIFICATION_RULE.PARENT_KIND)
                                .and(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(FLOW_CLASSIFICATION_RULE.PARENT_ID))));

        int[] updatedActorDecoratorRatings = dsl
                .select(LOGICAL_FLOW_DECORATOR.ID,
                        child_dt.ID,
                        FLOW_CLASSIFICATION.CODE,
                        FLOW_CLASSIFICATION_RULE.ID)
                .from(FLOW_CLASSIFICATION_RULE)
                .innerJoin(parent_dt).on(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.eq(parent_dt.ID))
                .innerJoin(ENTITY_HIERARCHY).on(parent_dt.ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.DATA_TYPE.name())))
                .innerJoin(child_dt).on(ENTITY_HIERARCHY.ID.eq(child_dt.ID))
                .innerJoin(LOGICAL_FLOW).on(logicalFlowTargetIsAuthSourceParent)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID)
                        .and(child_dt.ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))))
                .innerJoin(level).on(level.ID.eq(parent_dt.ID)
                        .and(level.ID.eq(level.ANCESTOR_ID)
                        .and(level.KIND.eq(EntityKind.DATA_TYPE.name()))))
                .innerJoin(FLOW_CLASSIFICATION).on(FLOW_CLASSIFICATION_RULE.FLOW_CLASSIFICATION_ID.eq(FLOW_CLASSIFICATION.ID))
                .where(FLOW_CLASSIFICATION.CODE.ne(LOGICAL_FLOW_DECORATOR.RATING).and(FLOW_CLASSIFICATION.DIRECTION.eq(direction.name())))
                .orderBy(level.LEVEL)
                .fetch()
                .stream()
                .map(r -> dsl
                        .update(LOGICAL_FLOW_DECORATOR)
                        .set(LOGICAL_FLOW_DECORATOR.RATING, r.get(FLOW_CLASSIFICATION.CODE))
                        .set(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID, r.get(FLOW_CLASSIFICATION_RULE.ID))
                        .where(LOGICAL_FLOW_DECORATOR.ID.eq(r.get(LOGICAL_FLOW_DECORATOR.ID))
                                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.eq(r.get(child_dt.ID))
                                        .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))))
                .collect(collectingAndThen(Collectors.toSet(), r -> dsl.batch(r).execute()));

        return IntStream.of(updatedActorDecoratorRatings).sum();
    }


    public Set<FlowClassificationRule> findCompanionEntityRules(long ruleId) {
        SelectConditionStep<Record2<Long, String>> sourceEntity = DSL
                .select(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID, FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND)
                .from(FLOW_CLASSIFICATION_RULE)
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(ruleId));

        return baseSelect()
                .innerJoin(sourceEntity)
                .on(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID.eq(sourceEntity.field(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID))
                        .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(sourceEntity.field(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND))
                                .and(FLOW_CLASSIFICATION_RULE.ID.ne(ruleId))))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<FlowClassificationRule> findCompanionDataTypeRules(long ruleId) {
        SelectConditionStep<Record1<Long>> dataTypeIds = DSL
                .select(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(FLOW_CLASSIFICATION_RULE)
                .innerJoin(ENTITY_HIERARCHY)
                .on(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.eq(ENTITY_HIERARCHY.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.DATA_TYPE.name())))
                .where(FLOW_CLASSIFICATION_RULE.ID.eq(ruleId));

        return baseSelect()
                .where(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.in(dataTypeIds)
                        .and(FLOW_CLASSIFICATION_RULE.ID.ne(ruleId)))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<FlowClassificationRule> findAppliedClassificationRulesForFlow(Long logicalFlowId) {
        SelectConditionStep<Record> outboundRules = baseSelect()
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW_DECORATOR.FLOW_CLASSIFICATION_RULE_ID.eq(FLOW_CLASSIFICATION_RULE.ID))
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(logicalFlowId));
        SelectConditionStep<Record> inboundRules = baseSelect()
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW_DECORATOR.INBOUND_FLOW_CLASSIFICATION_RULE_ID.eq(FLOW_CLASSIFICATION_RULE.ID))
                .where(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(logicalFlowId));
        return outboundRules
                .union(inboundRules)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public int updateDecoratorsWithClassifications(Set<UpdateConditionStep<LogicalFlowDecoratorRecord>> updateStmts) {
        return IntStream.of(dsl.batch(updateStmts).execute()).sum();
    }
}
