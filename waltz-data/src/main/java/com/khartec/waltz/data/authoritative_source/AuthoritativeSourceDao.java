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

package com.khartec.waltz.data.authoritative_source;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.*;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.records.AuthoritativeSourceRecord;
import com.khartec.waltz.schema.tables.records.EntityHierarchyRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


@Repository
public class AuthoritativeSourceDao {

    public final static Application CONSUMER_APP = APPLICATION.as("consumer");
    private final static Application SUPPLIER_APP = APPLICATION.as("supplier");
    private final static AggregateFunction<Integer> COUNT_FIELD = DSL.count(LOGICAL_FLOW);


    private final DSLContext dsl;

    private static final RecordMapper<Record, AuthoritativeSource> TO_DOMAIN_MAPPER = r -> {
        AuthoritativeSourceRecord record = r.into(AuthoritativeSourceRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        EntityReference orgUnitRef = ImmutableEntityReference.builder()
                .kind(EntityKind.ORG_UNIT)
                .id(r.getValue(ORGANISATIONAL_UNIT.ID))
                .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                .build();

        EntityReference appRef = ImmutableEntityReference.builder()
                .kind(EntityKind.APPLICATION)
                .id(r.getValue(SUPPLIER_APP.ID))
                .name(r.getValue(SUPPLIER_APP.NAME))
                .build();

        return ImmutableAuthoritativeSource.builder()
                .id(record.getId())
                .parentReference(parentRef)
                .appOrgUnitReference(orgUnitRef)
                .applicationReference(appRef)
                .dataType(record.getDataType())
                .rating(AuthoritativenessRating.valueOf(record.getRating()))
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .build();
    };


    private static final RecordMapper<Record, AuthoritativeRatingVantagePoint> TO_VANTAGE_MAPPER = r -> {
        AuthoritativeSourceRecord authRecord = r.into(AuthoritativeSourceRecord.class);
        EntityHierarchyRecord entityHierarchyRecord = r.into(EntityHierarchyRecord.class);

        return ImmutableAuthoritativeRatingVantagePoint.builder()
                .vantagePoint(ImmutableEntityReference.builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(entityHierarchyRecord.getId())
                        .build())
                .rank(entityHierarchyRecord.getLevel())
                .applicationId(authRecord.getApplicationId())
                .rating(AuthoritativenessRating.valueOf(authRecord.getRating()))
                .dataTypeCode(authRecord.getDataType())
                .build();
    };


    @Autowired
    public AuthoritativeSourceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<AuthoritativeSource> findByEntityKind(EntityKind kind) {
        checkNotNull(kind, "kind must not be null");
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(kind.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref must not be null");
        
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(ref.kind().name()))
                .and(AUTHORITATIVE_SOURCE.PARENT_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        checkTrue(applicationId > -1, "applicationId must be +ve");
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.APPLICATION_ID.eq(applicationId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int update(AuthoritativeSourceUpdateCommand command) {
        checkNotNull(command, "command cannot be null");
        checkTrue(command.id().isPresent(), "id must be +ve");

        UpdateSetMoreStep<AuthoritativeSourceRecord> upd = dsl.update(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.RATING, command.rating().name())
                .set(AUTHORITATIVE_SOURCE.DESCRIPTION, command.description());

        return upd
                .where(AUTHORITATIVE_SOURCE.ID.eq(command.id().get()))
                .execute();
    }


    public int insert(AuthoritativeSourceCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        SelectConditionStep<Record1<String>> dataTypeSelection = DSL
                .select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.eq(command.dataTypeId()));

        return dsl.insertInto(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.PARENT_KIND, EntityKind.ORG_UNIT.name())
                .set(AUTHORITATIVE_SOURCE.PARENT_ID, command.orgUnitId())
                .set(AUTHORITATIVE_SOURCE.DATA_TYPE, dataTypeSelection)
                .set(AUTHORITATIVE_SOURCE.APPLICATION_ID, command.applicationId())
                .set(AUTHORITATIVE_SOURCE.RATING, command.rating().name())
                .set(AUTHORITATIVE_SOURCE.DESCRIPTION, command.description())
                .set(AUTHORITATIVE_SOURCE.PROVENANCE, "waltz")
                .execute();
    }


    public int remove(long id) {
        return dsl.delete(AUTHORITATIVE_SOURCE)
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .execute();
    }


    public AuthoritativeSource getById(long id) {
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<AuthoritativeRatingVantagePoint> findAuthoritativeRatingVantagePoints(Set<Long> orgIds) {
        return dsl.select(
                    ENTITY_HIERARCHY.ID,
                    ENTITY_HIERARCHY.LEVEL,
                    AUTHORITATIVE_SOURCE.DATA_TYPE,
                    AUTHORITATIVE_SOURCE.APPLICATION_ID,
                    AUTHORITATIVE_SOURCE.RATING)
                .from(ENTITY_HIERARCHY)
                .join(AUTHORITATIVE_SOURCE)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID))
                .where(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())
                        .and(ENTITY_HIERARCHY.ID.in(orgIds)))
                .orderBy(ENTITY_HIERARCHY.ID, ENTITY_HIERARCHY.LEVEL)
                .fetch(TO_VANTAGE_MAPPER);
    }


    public List<AuthoritativeSource> findAll() {
        return baseSelect()
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            Select<Record1<Long>> dataTypeIdSelector) {

        SelectConditionStep<Record1<String>> dataTypeCodeSelector = DSL
                .select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.in(dataTypeIdSelector));

        Condition appJoin = APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                .and(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ENTITY_HIERARCHY.ID));

        Condition hierarchyJoin = ENTITY_HIERARCHY.ANCESTOR_ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition authSourceJoin = AUTHORITATIVE_SOURCE.APPLICATION_ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition dataFlowDecoratorJoin = LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID);

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeIdSelector)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(AUTHORITATIVE_SOURCE.DATA_TYPE.in(dataTypeCodeSelector))
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Field<Long> authSourceIdField = AUTHORITATIVE_SOURCE.ID.as("auth_source_id");
        Field<Long> applicationIdField = APPLICATION.ID.as("application_id");
        Field<String> applicationNameField = APPLICATION.NAME.as("application_name");

        Result<Record3<Long, Long, String>> records = dsl
                .select(authSourceIdField,
                        applicationIdField,
                        applicationNameField)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(dataFlowDecoratorJoin)
                .innerJoin(AUTHORITATIVE_SOURCE).on(authSourceJoin)
                .innerJoin(ENTITY_HIERARCHY).on(hierarchyJoin)
                .innerJoin(APPLICATION).on(appJoin)
                .where(condition)
                .orderBy(AUTHORITATIVE_SOURCE.ID, APPLICATION.NAME)
                .fetch();

        return groupBy(
                r -> mkRef(
                        EntityKind.AUTHORITATIVE_SOURCE,
                        r.getValue(authSourceIdField)),
                r -> mkRef(
                        EntityKind.APPLICATION,
                        r.getValue(applicationIdField),
                        r.getValue(applicationNameField)),
                records);
    }


    public List<EntityReference> cleanupOrphans() {
        Select<Record1<Long>> orgUnitIds = DSL
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT);
        Select<Record1<Long>> appIds = DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(IS_ACTIVE);

        Condition unknownOrgUnit = AUTHORITATIVE_SOURCE.PARENT_ID.notIn(orgUnitIds)
                .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition appIsInactive = AUTHORITATIVE_SOURCE.APPLICATION_ID.notIn(appIds);

        List<EntityReference> authSourceAppsWithoutOrgUnit = dsl
                .select(AUTHORITATIVE_SOURCE.APPLICATION_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(unknownOrgUnit)
                .fetch(AUTHORITATIVE_SOURCE.APPLICATION_ID)
                .stream()
                .map(id -> mkRef(EntityKind.APPLICATION, id))
                .collect(Collectors.toList());

        List<EntityReference> authSourceOrgUnitsWithoutApp = dsl
                .select(AUTHORITATIVE_SOURCE.PARENT_ID, AUTHORITATIVE_SOURCE.PARENT_KIND)
                .from(AUTHORITATIVE_SOURCE)
                .where(appIsInactive)
                .fetch()
                .stream()
                .map(r -> mkRef(
                        EntityKind.valueOf(r.get(AUTHORITATIVE_SOURCE.PARENT_KIND)),
                        r.get(AUTHORITATIVE_SOURCE.PARENT_ID)))
                .collect(Collectors.toList());

        List<EntityReference> bereaved = ListUtilities.concat(
                authSourceAppsWithoutOrgUnit,
                authSourceOrgUnitsWithoutApp);

        dsl.deleteFrom(AUTHORITATIVE_SOURCE)
                .where(unknownOrgUnit)
                .or(appIsInactive)
                .execute();

        return bereaved;
    }


    public List<NonAuthoritativeSource> findNonAuthSources(Condition customSelectionCriteria) {
        Condition flowNotRemoved = LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name());
        Condition supplierNotRemoved =  SUPPLIER_APP.IS_REMOVED.isFalse();
        Condition consumerNotRemoved =  CONSUMER_APP.IS_REMOVED.isFalse();
        Condition decorationIsAboutDataTypes = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name());
        Condition badFlow = LOGICAL_FLOW_DECORATOR.RATING.in(
                AuthoritativenessRating.DISCOURAGED.name(),
                AuthoritativenessRating.NO_OPINION.name());

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
                .where(customSelectionCriteria)
                .and(commonSelectionCriteria)
                .groupBy(SUPPLIER_APP.ID, SUPPLIER_APP.NAME, LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                .fetch()
                .map(r -> ImmutableNonAuthoritativeSource.builder()
                        .sourceReference(mkRef(
                                EntityKind.APPLICATION,
                                r.get(SUPPLIER_APP.ID),
                                r.get(SUPPLIER_APP.NAME)))
                        .dataTypeId(r.get(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID))
                        .count(r.get(COUNT_FIELD))
                        .build());
    }


    public List<AuthoritativeSource> findAuthSources(Condition customSelectionCriteria) {
        Condition criteria = AUTHORITATIVE_SOURCE.ID.in(DSL
                .select(AUTHORITATIVE_SOURCE.ID)
                .from(AUTHORITATIVE_SOURCE)
                    .innerJoin(LOGICAL_FLOW)
                    .on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID)
                            .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                            .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())))
                    .innerJoin(CONSUMER_APP)
                    .on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(CONSUMER_APP.ID)
                            .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                    .where(customSelectionCriteria));

        return baseSelect()
                .where(criteria)
                .fetch(TO_DOMAIN_MAPPER);
    }


    // -- HELPERS --

    private SelectOnConditionStep<Record> baseSelect() {
        return dsl
                .select(ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME)
                .select(AUTHORITATIVE_SOURCE.fields())
                .select(SUPPLIER_APP.NAME, SUPPLIER_APP.ID)
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(SUPPLIER_APP)
                .on(SUPPLIER_APP.ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID))
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(SUPPLIER_APP.ORGANISATIONAL_UNIT_ID));
    }

}
