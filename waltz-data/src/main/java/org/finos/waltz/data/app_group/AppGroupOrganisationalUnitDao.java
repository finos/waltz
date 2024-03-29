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

package org.finos.waltz.data.app_group;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.schema.tables.ApplicationGroupOuEntry.APPLICATION_GROUP_OU_ENTRY;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

@Repository
public class AppGroupOrganisationalUnitDao {
    private final DSLContext dsl;

    @Autowired
    public AppGroupOrganisationalUnitDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AppGroupEntry> getEntriesForGroup(long groupId) {
        return dsl
                .select(ORGANISATIONAL_UNIT.ID,
                        ORGANISATIONAL_UNIT.NAME,
                        ORGANISATIONAL_UNIT.DESCRIPTION)
                .select(APPLICATION_GROUP_OU_ENTRY.PROVENANCE,
                        APPLICATION_GROUP_OU_ENTRY.IS_READONLY)
                .from(ORGANISATIONAL_UNIT)
                .innerJoin(APPLICATION_GROUP_OU_ENTRY)
                .on(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(groupId))
                .fetch(r -> ImmutableAppGroupEntry
                        .builder()
                        .id(r.getValue(ORGANISATIONAL_UNIT.ID))
                        .name(r.getValue(ORGANISATIONAL_UNIT.NAME))
                        .description(r.getValue(ORGANISATIONAL_UNIT.DESCRIPTION))
                        .kind(EntityKind.ORG_UNIT)
                        .isReadOnly(r.getValue(APPLICATION_GROUP_OU_ENTRY.IS_READONLY))
                        .provenance(r.getValue(APPLICATION_GROUP_OU_ENTRY.PROVENANCE))
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
