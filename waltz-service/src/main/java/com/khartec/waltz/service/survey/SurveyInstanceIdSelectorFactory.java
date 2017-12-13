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

package com.khartec.waltz.service.survey;

import com.khartec.waltz.data.IdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableIdSelectionOptions;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.tables.SurveyInstance.SURVEY_INSTANCE;

@Service
public class SurveyInstanceIdSelectorFactory implements IdSelectorFactory {
    private final DSLContext dsl;

    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;


    @Autowired
    public SurveyInstanceIdSelectorFactory(DSLContext dsl,
                                           OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        this.dsl = dsl;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();

        switch (ref.kind()) {
            case APP_GROUP:
            case APPLICATION:
            case CHANGE_INITIATIVE:
                return mkForNonHierarchicalEntity(ref, options.scope());

            case ORG_UNIT:
                return mkForOrgUnit(ref, options.scope());

            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }



    private Select<Record1<Long>> mkForNonHierarchicalEntity(EntityReference ref, HierarchyQueryScope scope) {
        checkTrue(scope == EXACT, "Can only create selector for exact matches if given a non-hierarchical ref");

        return dsl
                .select(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ENTITY_KIND.eq(ref.kind().name())
                        .and(SURVEY_INSTANCE.ENTITY_ID.eq(ref.id())));
    }



    private SelectConditionStep<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {

        ImmutableIdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        return dsl
                .selectDistinct(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.ENTITY_KIND.eq(ref.kind().name())
                        .and(SURVEY_INSTANCE.ENTITY_ID.in(ouSelector)));
    }
}
