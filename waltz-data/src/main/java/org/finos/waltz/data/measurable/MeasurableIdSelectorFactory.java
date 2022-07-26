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

package org.finos.waltz.data.measurable;


import org.finos.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static org.finos.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static org.finos.waltz.data.SelectorUtilities.mkApplicationConditions;

public class MeasurableIdSelectorFactory implements IdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case PERSON:
                return mkForPerson(options);
            case MEASURABLE_CATEGORY:
                return mkForMeasurableCategory(options);
            case MEASURABLE:
                return mkForMeasurable(options);
            case APP_GROUP:
            case PROCESS_DIAGRAM:
                return mkViaAppSelector(options);
            case AGGREGATE_OVERLAY_DIAGRAM:
                return mkForAggregatedEntityDiagram(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case SCENARIO:
                return mkForScenario(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case ACTOR:
            case APPLICATION:
                return mkForDirectEntityKind(options);
            case CHANGE_INITIATIVE:
                return mkForChangeInitiative(options);
            default:
                throw new UnsupportedOperationException(format(
                        "Cannot create measurable selector from kind: %s",
                        options.entityReference().kind()));
        }
    }

    private Select<Record1<Long>> mkForChangeInitiative(IdSelectionOptions options) {

        Select<Record1<Long>> changeInitiativeSelector = new ChangeInitiativeIdSelectorFactory().apply(options);

        Select<Record1<Long>> ciToMeasurable = DSL
                .select(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(CHANGE_INITIATIVE)
                .on(CHANGE_INITIATIVE.ID.eq(ENTITY_RELATIONSHIP.ID_A)
                        .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name()))
                .and(CHANGE_INITIATIVE.ID.in(changeInitiativeSelector));

        Select<Record1<Long>> measurableToCI = DSL
                .select(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(CHANGE_INITIATIVE)
                .on(CHANGE_INITIATIVE.ID.eq(ENTITY_RELATIONSHIP.ID_B)
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.MEASURABLE.name()))
                .and(CHANGE_INITIATIVE.ID.in(changeInitiativeSelector));

        return measurableToCI
                .union(ciToMeasurable);

    }


    private Select<Record1<Long>> mkForAggregatedEntityDiagram(IdSelectionOptions options) {
        return null;
    }


    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        switch (options.scope()) {
            case CHILDREN:
                return mkForPersonReportees(options);
            default:
                throw new UnsupportedOperationException(
                        "Querying for measurable ids of person using (scope: '"
                                + options.scope()
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForPersonReportees(IdSelectionOptions options) {

        Select<Record1<String>> emp = DSL
                .select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(options.entityReference().id()));

        SelectConditionStep<Record1<String>> reporteeIds = DSL
                .selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(emp));

        return options.joiningEntityKind()
                .map(opt -> {
                    if (opt.equals(EntityKind.APPLICATION)){
                        Condition applicationConditions = mkApplicationConditions(options);

                        Condition condition = applicationConditions
                                .and(INVOLVEMENT.EMPLOYEE_ID.eq(emp)
                                        .or(INVOLVEMENT.EMPLOYEE_ID.in(reporteeIds)));

                        return mkBaseRatingBasedSelector()
                                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(MEASURABLE_RATING.ENTITY_ID))
                                .innerJoin(INVOLVEMENT).on(APPLICATION.ID.eq(INVOLVEMENT.ENTITY_ID)
                                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                                .where(condition)
                                .and(mkLifecycleCondition(options));
                    } else {
                        throw new UnsupportedOperationException(format(
                                "Cannot create measurable selector for people via entity kind: %s",
                                opt));
                    }
                })
                .orElse(DSL
                        .selectDistinct(MEASURABLE.ID)
                        .from(MEASURABLE)
                        .innerJoin(INVOLVEMENT).on(MEASURABLE.ID.eq(INVOLVEMENT.ENTITY_ID)
                                .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                        .where(INVOLVEMENT.EMPLOYEE_ID.in(reporteeIds).or(INVOLVEMENT.EMPLOYEE_ID.eq(emp)))
                        .and(mkLifecycleCondition(options)));
    }


    private Select<Record1<Long>> mkForMeasurableCategory(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(options.entityReference().id()))
                .and(mkLifecycleCondition(options));
    }


    private Select<Record1<Long>> mkForScenario(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .selectDistinct(SCENARIO_AXIS_ITEM.DOMAIN_ITEM_ID)
                .from(SCENARIO_AXIS_ITEM)
                .where(SCENARIO_AXIS_ITEM.SCENARIO_ID.eq(options.entityReference().id()))
                .and(SCENARIO_AXIS_ITEM.DOMAIN_ITEM_KIND.eq(EntityKind.MEASURABLE.name()));
    }


    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {
        Select<Record1<Long>> orgUnitSelector = orgUnitIdSelectorFactory.apply(options);
        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitSelector)
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID)))
                .and(mkLifecycleCondition(options));
    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        return mkViaAppSelector(options);
    }


    private Select<Record1<Long>> mkViaAppSelector(IdSelectionOptions options) {

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
        Select<Record1<Long>> validAppIds = applicationIdSelectorFactory.apply(options);

        SelectConditionStep<Record1<Long>> measurableIds = DSL
                .selectDistinct(MEASURABLE_RATING.MEASURABLE_ID)
                .from(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_ID.in(validAppIds));

        return DSL
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                .and(ENTITY_HIERARCHY.ID.in(measurableIds));
    }


    private Select<Record1<Long>> mkForDirectEntityKind(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate application based selectors with exact scopes");

        SelectConditionStep<Record1<Long>> measurablesViaReplacements = DSL
                .select(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID)
                .from(MEASURABLE_RATING_PLANNED_DECOMMISSION)
                .innerJoin(MEASURABLE_RATING_REPLACEMENT)
                .on(MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(MEASURABLE_RATING_REPLACEMENT.DECOMMISSION_ID))
                .where(MEASURABLE_RATING_REPLACEMENT.ENTITY_ID.eq(options.entityReference().id())
                        .and(MEASURABLE_RATING_REPLACEMENT.ENTITY_KIND.eq(options.entityReference().kind().name())));

        SelectConditionStep<Record1<Long>> measurablesViaRatings = DSL
                .select(MEASURABLE_RATING.MEASURABLE_ID)
                .from(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_ID.eq(options.entityReference().id())
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(options.entityReference().kind().name())));

        return DSL
                .selectDistinct(MEASURABLE.ID)
                .from(MEASURABLE)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(MEASURABLE.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())))
                .where(ENTITY_HIERARCHY.ID.in(measurablesViaRatings.union(measurablesViaReplacements)))
                .and(mkLifecycleCondition(options));
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate flow diagram based selectors with exact scopes");
        long diagramId = options.entityReference().id();
        Select<Record1<Long>> viaAppRatings = mkBaseRatingBasedSelector()
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(MEASURABLE_RATING.ENTITY_ID)
                        .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(MEASURABLE_RATING.ENTITY_KIND)))
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(mkLifecycleCondition(options));

        Select<Record1<Long>> viaDirectRelationship = DSL
                .select(FLOW_DIAGRAM_ENTITY.ENTITY_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(FLOW_DIAGRAM_ENTITY.ENTITY_ID))
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))
                .and(mkLifecycleCondition(options));

        return DSL.selectFrom(viaAppRatings.union(viaDirectRelationship).asTable());
    }



    /**
     * Returns ID's of all measurables (and their parents) related to a base set
     * of ids provided by joining to MEASURE_RATING.  Use this by adding on additional
     * joins or restrictions over the MEASURE_RATING table.
     */
    private SelectOnConditionStep<Record1<Long>> mkBaseRatingBasedSelector() {
        return DSL
                .selectDistinct(MEASURABLE.ID)
                .from(MEASURABLE)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(MEASURABLE.ID))
                .innerJoin(MEASURABLE_RATING)
                .on(MEASURABLE_RATING.MEASURABLE_ID.eq(ENTITY_HIERARCHY.ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
    }


    private Select<Record1<Long>> mkForMeasurable(IdSelectionOptions options) {
        if(options.joiningEntityKind().isPresent()) {
            return mkForIndirectMeasurable(options);
        } else {
            return mkForDirectMeasurable(options);
        }
    }


    private Select<Record1<Long>> mkForIndirectMeasurable(IdSelectionOptions options) {

        if(options.joiningEntityKind().get() != EntityKind.APPLICATION){
            throw new IllegalArgumentException(format(
                    "The joining entity kind: %s, cannot be used to indirectly selecting measurables",
                    options.joiningEntityKind().get()));
        } else {
            Select<Record1<Long>> selector = null;

            switch (options.scope()) {
                case CHILDREN:
                    Select<Record1<Long>> directMeasurableIds = mkForDirectMeasurable(options);

                    MeasurableRating dmr = MEASURABLE_RATING.as("directMeasurableRatings");

                    SelectConditionStep<Record1<Long>> directAppIds = DSL
                            .selectDistinct(dmr.ENTITY_ID)
                            .from(dmr)
                            .innerJoin(APPLICATION).on(dmr.ENTITY_ID.eq(APPLICATION.ID)
                                    .and(APPLICATION.IS_REMOVED.isFalse())
                                    .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())))
                            .where(dmr.MEASURABLE_ID.in(directMeasurableIds)
                                    .and(dmr.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

                    selector = mkBaseRatingBasedSelector()
                            .where(MEASURABLE_RATING.ENTITY_ID.in(directAppIds)
                            .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));

                    break;

                default:
                    throw new UnsupportedOperationException(format(
                            "Cannot create indirect measurable selector with scope: %s",
                            options.scope()));
            }

            return selector;
        }
    }


    private Select<Record1<Long>> mkForDirectMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> selector = null;
        final Condition isMeasurable = ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name());
        switch (options.scope()) {
            case EXACT:
                selector = DSL.select(DSL.val(options.entityReference().id()));
                break;
            case CHILDREN:
                selector = DSL
                        .select(ENTITY_HIERARCHY.ID)
                        .from(ENTITY_HIERARCHY)
                        .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(ENTITY_HIERARCHY.ID))
                        .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(options.entityReference().id()))
                        .and(isMeasurable)
                        .and(mkLifecycleCondition(options));
                break;
            case PARENTS:
                selector = DSL
                        .select(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .from(ENTITY_HIERARCHY)
                        .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(ENTITY_HIERARCHY.ANCESTOR_ID))
                        .where(ENTITY_HIERARCHY.ID.eq(options.entityReference().id()))
                        .and(isMeasurable)
                        .and(mkLifecycleCondition(options));
                break;
        }

        return selector;
    }


    private Condition mkLifecycleCondition(IdSelectionOptions options) {
        return MEASURABLE.ENTITY_LIFECYCLE_STATUS.in(options.entityLifecycleStatuses());
    }

}
