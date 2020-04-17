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

package com.khartec.waltz.data.measurable;


import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;

import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.data.SelectorUtilities.mkApplicationConditions;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static java.lang.String.format;

public class MeasurableIdSelectorFactory implements IdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    private final MeasurableIdDirectSelectorFactory measurableIdDirectSelectorFactory = new MeasurableIdDirectSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case PERSON:
                return mkForPerson(options);
            case APP_GROUP:
                return mkForAppGroup(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case ACTOR:
            case APPLICATION:
            case SCENARIO:
            case MEASURABLE:
            case MEASURABLE_CATEGORY:
                return measurableIdDirectSelectorFactory.apply(options);
            default:
                throw new UnsupportedOperationException(format(
                        "Cannot create measurable selector from kind: %s",
                        options.entityReference().kind()));
        }
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

        Select<Record1<String>> emp = DSL.select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .where(PERSON.ID.eq(options.entityReference().id()));

        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                .from(PERSON_HIERARCHY)
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(emp));

        Condition applicationConditions = mkApplicationConditions(options);

        Condition condition = applicationConditions
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(emp)
                        .or(INVOLVEMENT.EMPLOYEE_ID.in(reporteeIds)));

        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(MEASURABLE_RATING.ENTITY_ID))
                .innerJoin(INVOLVEMENT).on(APPLICATION.ID.eq(INVOLVEMENT.ENTITY_ID)
                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(condition);
    }


    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {
        Select<Record1<Long>> orgUnitSelector = orgUnitIdSelectorFactory.apply(options);
        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitSelector)
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID)));
    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate app-group based selectors with exact scopes");

        Select<Record1<Long>> validAppIdsInGroup = ApplicationIdSelectorFactory.mkForAppGroup(options);

        SelectConditionStep<Record1<Long>> measurableIdsUsedByGroup = DSL
                .selectDistinct(MEASURABLE_RATING.MEASURABLE_ID)
                .from(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.ENTITY_ID.in(validAppIdsInGroup));

        return DSL
                .selectDistinct(ENTITY_HIERARCHY.ANCESTOR_ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name()))
                .and(ENTITY_HIERARCHY.ID.in(measurableIdsUsedByGroup));
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate flow diagram based selectors with exact scopes");
        long diagramId = options.entityReference().id();
        Select<Record1<Long>> viaAppRatings = mkBaseRatingBasedSelector()
                .innerJoin(FLOW_DIAGRAM_ENTITY)
                .on(FLOW_DIAGRAM_ENTITY.ENTITY_ID.eq(MEASURABLE_RATING.ENTITY_ID)
                        .and(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(MEASURABLE_RATING.ENTITY_KIND)))
                .where(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId));

        Select<Record1<Long>> viaDirectRelationship = DSL
                .select(FLOW_DIAGRAM_ENTITY.ENTITY_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name()))
                .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId));

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
}
