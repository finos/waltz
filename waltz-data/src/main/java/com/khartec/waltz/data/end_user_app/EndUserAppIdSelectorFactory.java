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

import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.schema.tables.EndUserApplication;
import com.khartec.waltz.schema.tables.Involvement;
import com.khartec.waltz.schema.tables.Person;
import com.khartec.waltz.schema.tables.PersonHierarchy;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


@Service
public class EndUserAppIdSelectorFactory implements Function<ApplicationIdSelectionOptions, Select<Record1<Long>>>  {

    private static final Logger LOG = LoggerFactory.getLogger(EndUserAppIdSelectorFactory.class);

    private final DSLContext dsl;

    private final EndUserApplication eua = END_USER_APPLICATION.as("eua");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory;
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");


    @Autowired
    public EndUserAppIdSelectorFactory(DSLContext dsl,
                                       OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(orgUnitIdSelectorFactory, "orgUnitIdSelectorFactory cannot be null");

        this.dsl = dsl;
        this.orgUnitIdSelectorFactory = orgUnitIdSelectorFactory;
    }


    @Override
    public Select<Record1<Long>> apply(ApplicationIdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case PERSON:
                return mkForPerson(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }


    private Select<Record1<Long>> mkForOrgUnit(ApplicationIdSelectionOptions options) {

        if (!options.applicationKinds().contains(ApplicationKind.EUC)) {
            return mkEmptySelect();
        }

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(options);

        return dsl
                .selectDistinct(eua.ID)
                .from(eua)
                .where(dsl.renderInlined(eua.ORGANISATIONAL_UNIT_ID.in(ouSelector)));
    }


    private Select<Record1<Long>> mkForPerson(ApplicationIdSelectionOptions options) {
        switch (options.scope()) {
            case EXACT:
                return mkForSinglePerson(options);
            case CHILDREN:
                return mkForPersonReportees(options);
            default:
                throw new UnsupportedOperationException(
                        "Querying for appIds of person using (scope: '"
                                + options.scope()
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForSinglePerson(ApplicationIdSelectionOptions options) {

        if (!options.applicationKinds().contains(ApplicationKind.EUC)) {
            return mkEmptySelect();
        }

        Select<Record1<String>> employeeId = mkEmployeeIdSelect(options.entityReference());

        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(eua)
                .on(eua.ID.eq(involvement.ENTITY_ID))
                .where(involvement.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId));
    }


    private Select<Record1<Long>> mkForPersonReportees(ApplicationIdSelectionOptions options) {

        if (!options.applicationKinds().contains(ApplicationKind.EUC)) {
            return mkEmptySelect();
        }

        Select<Record1<String>> employeeId = mkEmployeeIdSelect(options.entityReference());
        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(employeeId));

        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name())
                .and(involvement.EMPLOYEE_ID.eq(employeeId)
                        .or(involvement.EMPLOYEE_ID.in(reporteeIds)));

        return dsl
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(eua)
                .on(eua.ID.eq(involvement.ENTITY_ID))
                .where(condition);
    }


    private Select<Record1<String>> mkEmployeeIdSelect(EntityReference ref) {
        return dsl.select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkEmptySelect() {
        return dsl.select(eua.ID)
                .from(eua)
                .where(DSL.falseCondition());
    }

}
