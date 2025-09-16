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

package org.finos.waltz.data.proposed_flow;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.person.PersonIdSelectorFactory;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.*;


public class ProposedFlowIdSelectorFactory implements IdSelectorFactory {

    private final PersonIdSelectorFactory personIdSelectorFactory = new PersonIdSelectorFactory();


    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch (options.scope()) {
            case CHILDREN:
                return idSelectorForUserWithHierarchy(options);
            case EXACT:
                return idSelectorForUser(options);
            default:
                throw new UnsupportedOperationException("Cannot create propose flow id selector from options: " + options);
        }
    }

    private Select<Record1<Long>> idSelectorForUserWithHierarchy(IdSelectionOptions options) {
        SelectConditionStep<Record1<String>> employeeIdForPerson = personIdSelectorFactory.getEmployeeIdForPerson(options);

        return DSL
                .select(PROPOSED_FLOW.ID)
                .from(PERSON)
                .join(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .and(PERSON_HIERARCHY.MANAGER_ID.eq(employeeIdForPerson))
                .and(PERSON.IS_REMOVED.isFalse())
                .join(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .join(INVOLVEMENT_KIND)
                .on(INVOLVEMENT.KIND_ID.eq(INVOLVEMENT_KIND.ID))
                .and(INVOLVEMENT_KIND.TRANSITIVE.isTrue())
                .join(PROPOSED_FLOW)
                .on(INVOLVEMENT.ENTITY_KIND.eq(PROPOSED_FLOW.TARGET_ENTITY_KIND))
                .and(INVOLVEMENT.ENTITY_ID.eq(PROPOSED_FLOW.TARGET_ENTITY_ID))
                .or(INVOLVEMENT.ENTITY_KIND.eq(PROPOSED_FLOW.SOURCE_ENTITY_KIND))
                .and(INVOLVEMENT.ENTITY_ID.eq(PROPOSED_FLOW.SOURCE_ENTITY_ID))
                .unionAll(idSelectorForUser(options)
                );
    }

    private Select<Record1<Long>> idSelectorForUser(IdSelectionOptions options) {
        SelectConditionStep<Record1<String>> employeeIdForPerson = personIdSelectorFactory.getEmployeeIdForPerson(options);

        return
                        DSL
                            .select(PROPOSED_FLOW.ID)
                            .from(PERSON)
                            .join(INVOLVEMENT)
                            .on(INVOLVEMENT.EMPLOYEE_ID.eq(employeeIdForPerson))
                            .join(PROPOSED_FLOW)
                            .on(INVOLVEMENT.ENTITY_KIND.eq(PROPOSED_FLOW.TARGET_ENTITY_KIND))
                            .and(INVOLVEMENT.ENTITY_ID.eq(PROPOSED_FLOW.TARGET_ENTITY_ID))
                            .or(INVOLVEMENT.ENTITY_KIND.eq(PROPOSED_FLOW.SOURCE_ENTITY_KIND)
                                    .and(INVOLVEMENT.ENTITY_ID.eq(PROPOSED_FLOW.SOURCE_ENTITY_ID))
                );
    }
}


