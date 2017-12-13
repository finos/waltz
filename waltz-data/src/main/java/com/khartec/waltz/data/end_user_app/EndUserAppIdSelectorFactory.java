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

package com.khartec.waltz.data.end_user_app;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.EntityIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableIdSelectionOptions;
import com.khartec.waltz.schema.tables.EndUserApplication;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

@Service
public class EndUserAppIdSelectorFactory extends EntityIdSelectorFactory  {


    private final EndUserApplication eua = END_USER_APPLICATION.as("eua");
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;


    @Autowired
    public EndUserAppIdSelectorFactory(DSLContext dsl,
                                       OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        super(dsl);
        Checks.checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    protected Select<Record1<Long>> mkForAppGroup(EntityReference ref, HierarchyQueryScope scope) {
        throw new IllegalArgumentException("App Group selectors not supported for End User Applications");
    }


    @Override
    protected Select<Record1<Long>> mkForOrgUnit(EntityReference ref, HierarchyQueryScope scope) {

        IdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        return dsl
                .selectDistinct(eua.ID)
                .from(eua)
                .where(dsl.renderInlined(eua.ORGANISATIONAL_UNIT_ID.in(ouSelector)));
    }

}
