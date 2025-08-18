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

package org.finos.waltz.data.maker_checker;


import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.InvolvementGroup.INVOLVEMENT_GROUP;
import static org.finos.waltz.schema.tables.InvolvementGroupEntry.INVOLVEMENT_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.tables.Person.PERSON;

@Repository
public class MakerCheckerPermissionDao {
    private final DSLContext dsl;
    @Autowired
    public MakerCheckerPermissionDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<Long> getInvolvementKindIdsFromInvolvementGroupEntry(String involvementGroupName){
        return dsl
                .select(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID)
                .from(INVOLVEMENT_GROUP_ENTRY)
                .innerJoin(INVOLVEMENT_GROUP).on(INVOLVEMENT_GROUP.ID.eq(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_GROUP_ID))
                .where(INVOLVEMENT_GROUP.NAME.eq(involvementGroupName))
                .fetch(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID);
    }

    public boolean getByEntityReferenceAndWorkflowId(String userEmailId, Long entityId, List<Long> involvementKindIds) {
        return dsl
                .select(INVOLVEMENT.fields())
                .from(INVOLVEMENT)
                .innerJoin(PERSON).on(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .where(PERSON.EMAIL.eq(userEmailId))
                .and(INVOLVEMENT.ENTITY_ID.eq(entityId))
                .and(INVOLVEMENT.KIND_ID.in(involvementKindIds))
                .fetch().isNotEmpty();
    }

    public List<Long> getIdsFromInvolvementKind(String involvmentKind, String subjectKind){
        return dsl
                .select(INVOLVEMENT_KIND.ID)
                .from(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.NAME.eq(involvmentKind))
                .and(INVOLVEMENT_KIND.SUBJECT_KIND.eq(subjectKind))
                .fetch(INVOLVEMENT_GROUP_ENTRY.INVOLVEMENT_KIND_ID);
    }

}
