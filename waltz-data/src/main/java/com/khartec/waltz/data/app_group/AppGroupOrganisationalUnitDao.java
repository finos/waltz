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

package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.ApplicationGroupOuEntry.APPLICATION_GROUP_OU_ENTRY;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class AppGroupOrganisationalUnitDao {
    private final DSLContext dsl;

    @Autowired
    public AppGroupOrganisationalUnitDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<EntityReference> getEntriesForGroup(long groupId) {
        Condition orgUnitsCondition = ORGANISATIONAL_UNIT.ID.in(DSL
                .select(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID)
                .from(APPLICATION_GROUP_OU_ENTRY)
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(groupId))
        );
        return dsl
                .select(
                        ORGANISATIONAL_UNIT.ID,
                        ORGANISATIONAL_UNIT.NAME,
                        ORGANISATIONAL_UNIT.DESCRIPTION)
                .from(ORGANISATIONAL_UNIT)
                .where(orgUnitsCondition)
                .fetch(r -> ImmutableEntityReference
                        .builder()
                        .id(r.getValue(ORGANISATIONAL_UNIT.ID))
                        .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                        .description(r.getValue(ORGANISATIONAL_UNIT.DESCRIPTION))
                        .kind(EntityKind.APPLICATION)
                        .build());
    }

    public int removeOrgUnit(long groupId, long orgUnitId) {
        return dsl.delete(APPLICATION_GROUP_OU_ENTRY)
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID.eq(orgUnitId))
                .execute();
    }

    public int addOrgUnit(long groupId, long orgUnitId) {
        return dsl.insertInto(APPLICATION_GROUP_OU_ENTRY)
                .set(APPLICATION_GROUP_OU_ENTRY.GROUP_ID, groupId)
                .set(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID, orgUnitId)
                .onDuplicateKeyIgnore()
                .execute();
    }
}
