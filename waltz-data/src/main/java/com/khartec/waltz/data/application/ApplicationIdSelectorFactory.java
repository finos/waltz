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

package com.khartec.waltz.data.application;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.NOT_REMOVED;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;

@Service
public class ApplicationIdSelectorFactory implements IdSelectorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationIdSelectorFactory.class);

    private final DSLContext dsl;
    private final DataTypeIdSelectorFactory dataTypeIdSelectorFactory;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;

    private final Application app = APPLICATION.as("app");
    private final ApplicationGroupEntry appGroup = APPLICATION_GROUP_ENTRY.as("appgrp");
    private final EntityRelationship relationship = ENTITY_RELATIONSHIP.as("relationship");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final MeasurableRating measurableRating = MEASURABLE_RATING.as("m_rating");
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");


    @Autowired
    public ApplicationIdSelectorFactory(DSLContext dsl,
                                        DataTypeIdSelectorFactory dataTypeIdSelectorFactory,
                                        MeasurableIdSelectorFactory measurableIdSelectorFactory, 
                                        OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(dataTypeIdSelectorFactory, "dataTypeIdSelectorFactory cannot be null");
        checkNotNull(measurableIdSelectorFactory, "measurableIdSelectorFactory cannot be null");
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");

        this.dsl = dsl;
        this.dataTypeIdSelectorFactory = dataTypeIdSelectorFactory;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case APP_GROUP:
                return mkForAppGroup(ref, options.scope());
            case APPLICATION:
                return mkForApplication(ref, options.scope());
            case CHANGE_INITIATIVE:
                return mkForChangeInitiative(ref, options.scope());
            case DATA_TYPE:
                return mkForDataType(options);
            case MEASURABLE:
                return mkForMeasurable(options);
            case ORG_UNIT:
                return mkForOrgUnit(ref, options.scope());
            case PERSON:
                return mkForPerson(ref, options.scope());
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }


    private Select<Record1<Long>> mkForMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> measurableSelector = measurableIdSelectorFactory.apply(options);
        return dsl.select(measurableRating.ENTITY_ID)
                .from(measurableRating)
                .where(measurableRating.ENTITY_KIND.eq(DSL.val(EntityKind.APPLICATION.name())))
                .and(measurableRating.MEASURABLE_ID.in(measurableSelector));
    }


    private Select<Record1<Long>> mkForApplication(EntityReference ref, HierarchyQueryScope scope) {
        checkTrue(scope == EXACT, "Can only create selector for exact matches if given an app ref");
        return DSL.select(DSL.val(ref.id()));
    }


    private Select<Record1<Long>> mkForChangeInitiative(EntityReference ref, HierarchyQueryScope scope) {
        checkTrue(scope == EXACT, "Can only create selector for exact matches if given a change initiative");

        Select<Record1<Long>> appToCi = DSL.selectDistinct(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()));

        Select<Record1<Long>> ciToApp = DSL.selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));


        return appToCi
                .union(ciToApp);
    }


    private SelectConditionStep<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {

        ImmutableIdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        return dsl
                .selectDistinct(app.ID)
                .from(app)
                .where(dsl.renderInlined(app.ORGANISATIONAL_UNIT_ID.in(ouSelector)));
    }


    private SelectConditionStep<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        if (scope != EXACT) {
            LOG.info("App Groups are not hierarchical therefore ignoring requested scope of: " + scope);
        }
        return dsl
                .selectDistinct(appGroup.APPLICATION_ID)
                .from(appGroup)
                .where(appGroup.GROUP_ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkForPerson(EntityReference ref, HierarchyQueryScope scope) {
        switch (scope) {
            case EXACT:
                return mkForSinglePerson(ref);
            case CHILDREN:
                return mkForPersonReportees(ref);
            default:
                throw new UnsupportedOperationException(
                        "Querying for appIds of person using (scope: '"
                                + scope
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForPersonReportees(EntityReference ref) {

        Select<Record1<String>> emp = dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()));

        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(emp));

        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(involvement.EMPLOYEE_ID.eq(emp)
                        .or(involvement.EMPLOYEE_ID.in(reporteeIds)));

        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .where(dsl.renderInlined(condition));
    }


    private Select<Record1<Long>> mkForSinglePerson(EntityReference ref) {

        Select<Record1<String>> employeeId = dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()));

        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .where(involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId));
    }


    private Select<Record1<Long>> mkForDataType(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeSelector = dataTypeIdSelectorFactory.apply(options);

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeSelector)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()));

        Field appId = DSL.field("app_id", Long.class);

        SelectConditionStep<Record1<Long>> sources = selectLogicalFlowAppsByDataType(
                LOGICAL_FLOW.SOURCE_ENTITY_ID.as(appId),
                condition.and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

        SelectConditionStep<Record1<Long>> targets = selectLogicalFlowAppsByDataType(
                LOGICAL_FLOW.TARGET_ENTITY_ID.as(appId),
                condition.and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

        return dsl.selectDistinct(appId)
                .from(sources)
                .union(targets);
    }




    private SelectConditionStep<Record1<Long>> selectLogicalFlowAppsByDataType(Field<Long> appField, Condition condition) {
        return dsl
                .select(appField)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR)
                .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where(dsl.renderInlined(condition))
                .and(NOT_REMOVED);
    }

}