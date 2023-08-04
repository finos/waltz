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

package org.finos.waltz.data.end_user_app;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.schema.tables.EndUserApplication;
import org.finos.waltz.schema.tables.Involvement;
import org.finos.waltz.schema.tables.Person;
import org.finos.waltz.schema.tables.PersonHierarchy;
import org.finos.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.application.ApplicationKind;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static org.finos.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static org.finos.waltz.common.Checks.checkNotNull;

public class EndUserAppIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>>  {

    private static final Logger LOG = LoggerFactory.getLogger(EndUserAppIdSelectorFactory.class);

    private final EndUserApplication eua = END_USER_APPLICATION.as("eua");
    private final Involvement involvement = INVOLVEMENT.as("involvement");
    private final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    private final Person person = PERSON.as("per");
    private final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("phier");
    private final Set<String> validLifecyclePhases = SetUtilities.asSet(
            LifecyclePhase.PRODUCTION.name(),
            LifecyclePhase.DEVELOPMENT.name());


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
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


    private Select<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {

        if (eucOmitted(options)) {
            return mkEmptySelect();
        }

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(options);

        return DSL
                .selectDistinct(eua.ID)
                .from(eua)
                .where(eua.ORGANISATIONAL_UNIT_ID.in(ouSelector)
                        .and(eua.LIFECYCLE_PHASE.in(validLifecyclePhases)));
    }


    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        switch (options.scope()) {
            case EXACT:
                return mkForSinglePerson(options);
            case CHILDREN:
                return mkForPersonReportees(options);
            default:
                throw new UnsupportedOperationException(
                        "Querying for endUserAppIds of person using (scope: '"
                                + options.scope()
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForSinglePerson(IdSelectionOptions options) {

        if (eucOmitted(options)) {
            return mkEmptySelect();
        }

        Select<Record1<String>> employeeId = mkEmployeeIdSelect(options.entityReference());

        return DSL
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(eua)
                .on(eua.ID.eq(involvement.ENTITY_ID))
                .where(involvement.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId))
                .and(eua.LIFECYCLE_PHASE.in(validLifecyclePhases));
    }


    private Select<Record1<Long>> mkForPersonReportees(IdSelectionOptions options) {

        if (eucOmitted(options)) {
            return mkEmptySelect();
        }

        Select<Record1<String>> employeeId = mkEmployeeIdSelect(options.entityReference());
        SelectConditionStep<Record1<String>> reporteeIds = DSL.selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(employeeId));

        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name())
                .and(involvement.EMPLOYEE_ID.eq(employeeId)
                        .or(involvement.EMPLOYEE_ID.in(reporteeIds)));

        return DSL
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(eua)
                .on(eua.ID.eq(involvement.ENTITY_ID))
                .where(condition)
                .and(eua.LIFECYCLE_PHASE.in(validLifecyclePhases));
    }


    private boolean eucOmitted(IdSelectionOptions options) {
        return options.filters().omitApplicationKinds().contains(ApplicationKind.EUC);
    }


    private Select<Record1<String>> mkEmployeeIdSelect(EntityReference ref) {
        return DSL
                .select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(ref.id()));
    }


    private Select<Record1<Long>> mkEmptySelect() {
        return DSL
                .select(eua.ID)
                .from(eua)
                .where(DSL.falseCondition());
    }

}
