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

package com.khartec.waltz.data.measurable;


import com.khartec.waltz.data.entity_hierarchy.AbstractIdSelectorFactory;
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
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static java.lang.String.format;

@Service
public class MeasurableIdSelectorFactory extends AbstractIdSelectorFactory {

    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;
    
    @Autowired
    public MeasurableIdSelectorFactory(DSLContext dsl, 
                                       OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        super(dsl, EntityKind.MEASURABLE);
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    protected Select<Record1<Long>> mkForOptions(IdSelectionOptions options) {
        switch (options.entityReference().kind()) {
            case APP_GROUP:
                return mkForAppGroup(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            default:
                throw new UnsupportedOperationException(format(
                        "Cannot create measurable selector from kind: %s",
                        options.entityReference().kind()));
        }
    }

    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {
        Select<Record1<Long>> orgUnitSelector = orgUnitIdSelectorFactory.apply(options);
        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitSelector)
                        .and(MEASURABLE_RATING.ENTITY_ID.eq(APPLICATION.ID))
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
    }


    private Select<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        checkTrue(options.scope() == HierarchyQueryScope.EXACT, "Can only calculate app-group based selectors with exact scopes");
        return mkBaseRatingBasedSelector()
                .innerJoin(APPLICATION_GROUP_ENTRY)
                .on(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(MEASURABLE_RATING.ENTITY_ID)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(options.entityReference().id()));
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
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.MEASURABLE.name())));
    }

}
