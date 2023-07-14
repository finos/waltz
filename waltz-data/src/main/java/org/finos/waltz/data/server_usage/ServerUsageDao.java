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

package org.finos.waltz.data.server_usage;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.server_usage.ImmutableServerUsage;
import org.finos.waltz.model.server_usage.ServerUsage;
import org.finos.waltz.schema.tables.records.ServerUsageRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.schema.tables.ServerUsage.SERVER_USAGE;


@Repository
public class ServerUsageDao {

    private static final String PROVENANCE = "waltz";
    private final DSLContext dsl;


    private final RecordMapper<Record, ServerUsage> TO_DOMAIN_MAPPER = r -> {
        ServerUsageRecord row = r.into(ServerUsageRecord.class);

        EntityKind entityKind = readEnum(row.getEntityKind(), EntityKind.class, s -> null);

        return ImmutableServerUsage.builder()
                .id(row.getId())
                .serverId(row.getServerId())
                .environment(row.getEnvironment())
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


    public Collection<ServerUsage> findByServerId(long serverId) {
        return findByCondition(SERVER_USAGE.SERVER_ID.eq(serverId));
    }


    public Collection<ServerUsage> findByReferencedEntity(EntityReference ref) {
        return findByCondition(
                SERVER_USAGE.ENTITY_ID.eq(ref.id())
                        .and(SERVER_USAGE.ENTITY_KIND.eq(ref.kind().name())));
    }


    // -- helpers ---

    private List<ServerUsage> findByCondition(Condition condition) {
        checkNotNull(condition, "condition cannot be null");
        return dsl
                .select(SERVER_USAGE.fields())
                .from(SERVER_USAGE)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
