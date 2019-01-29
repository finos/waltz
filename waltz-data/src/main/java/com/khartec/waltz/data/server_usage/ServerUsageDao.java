/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.data.server_usage;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.server_usage.ImmutableServerUsage;
import com.khartec.waltz.model.server_usage.ServerUsage;
import com.khartec.waltz.schema.tables.records.ServerUsageRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static com.khartec.waltz.schema.tables.ServerUsage.SERVER_USAGE;


@Repository
public class ServerUsageDao {

    private static final String PROVENANCE = "waltz";
    private final DSLContext dsl;


    private final RecordMapper<Record, ServerUsage> TO_DOMAIN_MAPPER = r -> {
        ServerUsageRecord row = r.into(ServerUsageRecord.class);

        EntityKind entityKind = readEnum(row.getEntityKind(), EntityKind.class, s -> null);

        return ImmutableServerUsage.builder()
                .serverId(row.getServerId())
                .entityReference(EntityReference.mkRef(entityKind, row.getEntityId()))
                .lastUpdatedBy(row.getLastUpdatedBy())
                .lastUpdatedAt(row.getLastUpdatedAt().toLocalDateTime())
                .provenance(row.getProvenance())
                .build();
    };


    private final Function<ServerUsage, ServerUsageRecord> TO_RECORD_MAPPER = r -> {

        ServerUsageRecord record = new ServerUsageRecord();
        record.setServerId(r.serverId());
        record.setEntityKind(r.entityReference().kind().name());
        record.setEntityId(r.entityReference().id());
        record.setLastUpdatedAt(nowUtcTimestamp());
        record.setLastUpdatedBy(r.lastUpdatedBy());
        record.setProvenance(r.provenance());
        return record;
    };


    @Autowired
    public ServerUsageDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ServerUsage> findForIdSelector(EntityKind kind, Select<Record1<Long>> selector) {
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(selector, "selector cannot be null");
        return findByCondition(
                SERVER_USAGE.ENTITY_KIND.eq(kind.name())
                        .and(SERVER_USAGE.ENTITY_ID.in(selector)));
    }


    public List<ServerUsage> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return findByCondition(
                SERVER_USAGE.ENTITY_KIND.eq(ref.kind().name())
                        .and(SERVER_USAGE.ENTITY_ID.eq(ref.id())));
    }


    private List<ServerUsage> findByCondition(Condition condition) {
        checkNotNull(condition, "condition cannot be null");
        return dsl
                .select(SERVER_USAGE.fields())
                .from(SERVER_USAGE)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
