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

package com.khartec.waltz.data.app_group;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;


@Repository
public class AppGroupEntryDao {

    private static final RecordMapper<Record, EntityReference> appRefMapper = r ->
            ImmutableEntityReference.builder()
                    .kind(EntityKind.APPLICATION)
                    .id(r.getValue(APPLICATION.ID))
                    .name(r.getValue(APPLICATION.NAME))
                    .build();

    private final DSLContext dsl;


    @Autowired
    public AppGroupEntryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    @Deprecated
    public List<EntityReference> getEntriesForGroup(long groupId) {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(DSL.select(APPLICATION_GROUP_ENTRY.APPLICATION_ID)
                        .from(APPLICATION_GROUP_ENTRY)
                        .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))))
                .and(IS_ACTIVE)
                .fetch(appRefMapper);
    }

    public int addApplication(long groupId, long applicationId) {
        return dsl.insertInto(APPLICATION_GROUP_ENTRY)
                .set(APPLICATION_GROUP_ENTRY.GROUP_ID, groupId)
                .set(APPLICATION_GROUP_ENTRY.APPLICATION_ID, applicationId)
                .onDuplicateKeyIgnore()
                .execute();
    }


    public int[] addApplications(long groupId, List<Long> applicationIds) {
        Query[] queries = applicationIds
                .stream()
                .map(id -> DSL.insertInto(APPLICATION_GROUP_ENTRY)
                        .set(APPLICATION_GROUP_ENTRY.GROUP_ID, groupId)
                        .set(APPLICATION_GROUP_ENTRY.APPLICATION_ID, id)
                        .onDuplicateKeyIgnore())
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
    }


    public int removeApplication(long groupId, long applicationId) {
        return dsl.delete(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_ENTRY.APPLICATION_ID.eq(applicationId))
                .execute();
    }


    public int removeApplications(long groupId, List<Long> applicationIds) {
        return dsl.delete(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(groupId))
                .and(APPLICATION_GROUP_ENTRY.APPLICATION_ID.in(applicationIds))
                .execute();
    }
}
