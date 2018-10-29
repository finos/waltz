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

package com.khartec.waltz.data.measurable;


import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.Tables.SCENARIO_AXIS_ITEM;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static java.lang.String.format;

@Service
public class MeasurableIdSelectorFactory implements IdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;
    private final DSLContext dsl;

    @Autowired
    public MeasurableIdSelectorFactory(DSLContext dsl, 
                                       OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case MEASURABLE_CATEGORY:
                return mkForMeasurableCategory(options);
            case MEASURABLE:
                return mkForMeasurable(options);
            case ACTOR:
            case APPLICATION:
                return mkForDirectEntityKind(options);
            case APP_GROUP:
                return mkForAppGroup(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case SCENARIO:
                return mkForScenario(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            default:
                throw new UnsupportedOperationException(format(
                        "Cannot create measurable selector from kind: %s",
                        options.entityReference().kind()));
        }
    }


    private Select<Record1<Long>> mkForMeasurableCategory(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        return DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(options.entityReference().id()));
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
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID)));
    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate app-group based selectors with exact scopes");
        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION_GROUP_ENTRY)
                .on(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(MEASURABLE_RATING.ENTITY_ID))
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForDirectEntityKind(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate application based selectors with exact scopes");
        return mkBaseRatingBasedSelector()
                .where(MEASURABLE_RATING.ENTITY_ID.in(options.entityReference().id()))
                .and(MEASURABLE_RATING.ENTITY_KIND.eq(options.entityReference().kind().name()));
    }


    /**
     * Returns all measurables liked to entities listed in the diagram
     *
     * @param options
     * @return
     */
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

        return viaAppRatings.union(viaDirectRelationship);

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
        Select<Record1<Long>> selector = null;
        final Condition isMeasurable = ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name());
        switch (options.scope()) {
            case EXACT:
                selector = DSL.select(DSL.val(options.entityReference().id()));
                break;
            case CHILDREN:
                selector = DSL.select(ENTITY_HIERARCHY.ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(options.entityReference().id()))
                        .and(isMeasurable);
                break;
            case PARENTS:
                selector = DSL.select(ENTITY_HIERARCHY.ANCESTOR_ID)
                        .from(ENTITY_HIERARCHY)
                        .where(ENTITY_HIERARCHY.ID.eq(options.entityReference().id()))
                        .and(isMeasurable);
                break;
        }

        return selector;
    }

}
