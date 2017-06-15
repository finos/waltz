/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.authoritativesource.ImmutableAuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.authoritativesource.ImmutableAuthoritativeSource;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.schema.tables.DataType;
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

    private static LogicalFlow lf = LOGICAL_FLOW.as("lf");
    private static DataType dt = DATA_TYPE.as("dt");
    private static LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");
    private static EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");
    private static Application app = APPLICATION.as("app");
    private static com.khartec.waltz.schema.tables.AuthoritativeSource au = AUTHORITATIVE_SOURCE.as("au");

    private final DSLContext dsl;

    private static final RecordMapper<Record, AuthoritativeSource> TO_AUTH_SOURCE_MAPPER = r -> {
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
                .id(r.getValue(APPLICATION.ID))
                .name(r.getValue(APPLICATION.NAME))
                .build();

        return ImmutableAuthoritativeSource.builder()
                .id(record.getId())
                .parentReference(parentRef)
                .appOrgUnitReference(orgUnitRef)
                .applicationReference(appRef)
                .dataType(record.getDataType())
                .rating(AuthoritativenessRating.valueOf(record.getRating()))
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
                .fetch(TO_AUTH_SOURCE_MAPPER);
    }


    public List<AuthoritativeSource> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref must not be null");
        
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(ref.kind().name()))
                .and(AUTHORITATIVE_SOURCE.PARENT_ID.eq(ref.id()))
                .fetch(TO_AUTH_SOURCE_MAPPER);
    }


    private SelectOnConditionStep<Record> baseSelect() {
        return dsl.select()
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID))
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID));
    }


    public List<AuthoritativeSource> findByApplicationId(long applicationId) {
        checkTrue(applicationId > -1, "applicationId must be +ve");
        
        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.APPLICATION_ID.eq(applicationId))
                .fetch(TO_AUTH_SOURCE_MAPPER);
    }


    public int update(long id, AuthoritativenessRating rating) {
        checkTrue(id > -1, "id must be +ve");
        checkNotNull(rating, "rating must not be null");

        return dsl.update(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.RATING, rating.name())
                .where(AUTHORITATIVE_SOURCE.ID.eq(id))
                .execute();
    }


    public int insert(EntityReference parentRef, String dataType, Long appId, AuthoritativenessRating rating) {
        checkNotNull(parentRef, "parentRef must not be null");
        Checks.checkNotEmpty(dataType, "dataType cannot be empty");
        checkNotNull(rating, "rating must not be null");

        return dsl.insertInto(AUTHORITATIVE_SOURCE)
                .set(AUTHORITATIVE_SOURCE.PARENT_KIND, parentRef.kind().name())
                .set(AUTHORITATIVE_SOURCE.PARENT_ID, parentRef.id())
                .set(AUTHORITATIVE_SOURCE.DATA_TYPE, dataType)
                .set(AUTHORITATIVE_SOURCE.APPLICATION_ID, appId)
                .set(AUTHORITATIVE_SOURCE.RATING, rating.name())
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
                .fetchOne(TO_AUTH_SOURCE_MAPPER);
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
                .fetch(TO_AUTH_SOURCE_MAPPER);
    }


    public List<AuthoritativeSource> findByDataTypeIdSelector(Select<Record1<Long>> selector) {
        SelectConditionStep<Record1<String>> codeSelector = DSL
                .select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .where(DATA_TYPE.ID.in(selector));

        return baseSelect()
                .where(AUTHORITATIVE_SOURCE.DATA_TYPE.in(codeSelector))
                .fetch(TO_AUTH_SOURCE_MAPPER);
    }


    public Map<EntityReference, Collection<EntityReference>> calculateConsumersForDataTypeIdSelector(
            Select<Record1<Long>> selector) {

        SelectConditionStep<Record1<String>> dataTypeCodeSelector = DSL
                .select(dt.CODE)
                .from(dt)
                .where(dt.ID.in(selector));

        Condition appJoin = app.ID.eq(lf.TARGET_ENTITY_ID)
                .and(app.ORGANISATIONAL_UNIT_ID.eq(eh.ID));

        Condition hierarchyJoin = eh.ANCESTOR_ID.eq(au.PARENT_ID)
                .and(eh.KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition authSourceJoin = au.APPLICATION_ID.eq(lf.SOURCE_ENTITY_ID)
                .and(lf.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition dataFlowDecoratorJoin = lfd.LOGICAL_FLOW_ID.eq(lf.ID);

        Condition condition = lfd.DECORATOR_ENTITY_ID.in(selector)
                .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(au.DATA_TYPE.in(dataTypeCodeSelector));

        Condition notRemoved = lf.IS_REMOVED.isFalse();

        Field<Long> authSourceIdField = au.ID.as("auth_source_id");
        Field<Long> applicationIdField = app.ID.as("application_id");
        Field<String> applicationNameField = app.NAME.as("application_name");

        Result<Record3<Long, Long, String>> records = dsl
                .select(authSourceIdField,
                        applicationIdField,
                        applicationNameField)
                .from(lf)
                .innerJoin(lfd).on(dataFlowDecoratorJoin)
                .innerJoin(au).on(authSourceJoin)
                .innerJoin(eh).on(hierarchyJoin)
                .innerJoin(app).on(appJoin)
                .where(condition)
                .and(notRemoved)
                .orderBy(au.ID, app.NAME)
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
                .from(APPLICATION);

        Condition unknownOrgUnit = AUTHORITATIVE_SOURCE.PARENT_ID.notIn(orgUnitIds)
                .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

        Condition unknownApp = AUTHORITATIVE_SOURCE.APPLICATION_ID.notIn(appIds);

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
                .where(unknownApp)
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
                .or(unknownApp)
                .execute();

        return bereaved;
    }
}
