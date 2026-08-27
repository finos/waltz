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

package org.finos.waltz.data.database_usage;

import org.finos.waltz.schema.tables.records.DatabaseUsageRecord;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.database_usage.DatabaseUsage;
import org.finos.waltz.model.database_usage.DatabaseUsageCreateCommand;
import org.finos.waltz.model.database_usage.ImmutableDatabaseUsage;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.tables.DatabaseUsage.DATABASE_USAGE;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.EnumUtilities.readEnum;


@Repository
public class DatabaseUsageDao {

    private static final String PROVENANCE = "waltz";
    private final DSLContext dsl;


    private final RecordMapper<Record, DatabaseUsage> TO_DOMAIN_MAPPER = r -> {
        DatabaseUsageRecord row = r.into(DatabaseUsageRecord.class);

        EntityKind entityKind = readEnum(row.getEntityKind(), EntityKind.class, s -> null);

        return ImmutableDatabaseUsage.builder()
                .id(row.getId())
                .databaseId(row.getDatabaseId())
                .entityReference(EntityReference.mkRef(entityKind, row.getEntityId()))
                .environment(row.getEnvironment())
                .lastUpdatedBy(row.getLastUpdatedBy())
                .lastUpdatedAt(row.getLastUpdatedAt().toLocalDateTime())
                .provenance(row.getProvenance())
                .build();
    };

    @Autowired
    public DatabaseUsageDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public Collection<DatabaseUsage> findByDatabaseId(long databaseId) {
        return findByCondition(DATABASE_USAGE.DATABASE_ID.eq(databaseId));
    }


    public Collection<DatabaseUsage> findByEntityReference(EntityReference ref) {
        return findByCondition(
                DATABASE_USAGE.ENTITY_ID.eq(ref.id())
                        .and(DATABASE_USAGE.ENTITY_KIND.eq(ref.kind().name())));
    }


    /**
     * Links the given database assets to the referenced entity in the requested environments.
     * Replaces any existing links between the entity and the same databases (making the call
     * idempotent), then inserts the new ones. Returns the number of links written.
     */
    public int create(EntityReference ref, Collection<DatabaseUsageCreateCommand> commands, String username) {
        checkNotNull(ref, "ref cannot be null");
        checkNotNull(commands, "commands cannot be null");

        Set<Long> databaseIds = commands
                .stream()
                .map(DatabaseUsageCreateCommand::databaseId)
                .collect(Collectors.toSet());

        dsl.deleteFrom(DATABASE_USAGE)
                .where(DATABASE_USAGE.ENTITY_KIND.eq(ref.kind().name()))
                .and(DATABASE_USAGE.ENTITY_ID.eq(ref.id()))
                .and(DATABASE_USAGE.DATABASE_ID.in(databaseIds))
                .execute();

        List<DatabaseUsageRecord> records = commands
                .stream()
                .map(c -> {
                    DatabaseUsageRecord record = new DatabaseUsageRecord();
                    record.setDatabaseId(c.databaseId());
                    record.setEntityKind(ref.kind().name());
                    record.setEntityId(ref.id());
                    record.setEnvironment(c.environment());
                    record.setProvenance(PROVENANCE);
                    record.setLastUpdatedBy(username);
                    record.setLastUpdatedAt(nowUtcTimestamp());
                    return record;
                })
                .collect(Collectors.toList());

        dsl.batchInsert(records).execute();
        return records.size();
    }

    // -- helpers ---
    private List<DatabaseUsage> findByCondition(Condition condition) {
        return dsl
                .select(DATABASE_USAGE.fields())
                .from(DATABASE_USAGE)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
