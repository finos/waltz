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

package org.finos.waltz.data.change_initiative;

import org.finos.waltz.data.SelectorUtilities;
import org.finos.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;

public class ChangeInitiativeIdSelectorFactory extends AbstractIdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();


    public ChangeInitiativeIdSelectorFactory() {
        super(EntityKind.CHANGE_INITIATIVE);
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case ALL:
                return mkForAll(options);
            case ACTOR:
            case APPLICATION:
            case MEASURABLE:
            case SCENARIO:
                return mkForRef(options);
            case APP_GROUP:
                return mkForAppGroup(options);
            case PERSON:
                return mkForPerson(options);
            case CHANGE_INITIATIVE:
                return mkForChangeInitiative(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            default:
                String msg = String.format(
                        "Cannot create Change Initiative Id selector from kind: %s",
                        options.entityReference().kind());
                throw new UnsupportedOperationException(msg);
        }
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL
                .selectDistinct(FLOW_DIAGRAM_ENTITY.ENTITY_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        SelectConditionStep<Record1<String>> empIdSelector = DSL
                .selectDistinct(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(options.entityReference().id()));

        return DSL.selectDistinct(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(CHANGE_INITIATIVE.ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(INVOLVEMENT.EMPLOYEE_ID.in(empIdSelector));
    }


    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {
        Select<Record1<Long>> ouSelector = organisationalUnitIdSelectorFactory.apply(options);

        EntityHierarchy ciHierarchy = ENTITY_HIERARCHY.as("ciHierarchy");

        if (options.scope() == HierarchyQueryScope.EXACT) {
            return DSL
                    .selectDistinct(CHANGE_INITIATIVE.ID)
                    .from(CHANGE_INITIATIVE)
                    .where(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID.in(ouSelector));
        } else {
            return DSL
                    .selectDistinct(ciHierarchy.ID)
                    .from(ciHierarchy)
                    .innerJoin(CHANGE_INITIATIVE).on(CHANGE_INITIATIVE.ID.eq(ciHierarchy.ANCESTOR_ID)
                            .and(ciHierarchy.KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                    .where(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID.in(ouSelector));
        }
    }


    private Select<Record1<Long>> mkForChangeInitiative(IdSelectionOptions options) {
        SelectorUtilities.ensureScopeIsExact(options);
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForAll(IdSelectionOptions options) {
        return DSL
                .select(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.LIFECYCLE_PHASE.ne(LifecyclePhase.RETIRED.name()));
    }


    private Select<Record1<Long>> mkForRef(IdSelectionOptions options) {
        EntityReference ref = options.entityReference();

        Select<Record1<Long>> aToB = DSL
                .selectDistinct(ENTITY_RELATIONSHIP.ID_A.as("id"))
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(ref.id()));

        Select<Record1<Long>> bToA = DSL
                .selectDistinct(ENTITY_RELATIONSHIP.ID_B.as("id"))
                .from(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(ref.kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(ref.id()));

        return DSL.selectFrom(aToB.union(bToA).asTable());
    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {

        Select<Record1<Long>> directlyRelatedCis = mkForRef(options);

        ImmutableIdSelectionOptions hierarchySelector = ImmutableIdSelectionOptions.copyOf(options)
                .withScope(HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> indirectlyRelatedCis = mkForOrgUnit(hierarchySelector);

        return DSL.selectFrom(directlyRelatedCis.union(indirectlyRelatedCis).asTable());
    }

}
