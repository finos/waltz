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

package org.finos.waltz.data.actor;

import org.finos.waltz.schema.tables.records.ActorRecord;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.actor.ActorChangeCommand;
import org.finos.waltz.model.actor.ActorCreateCommand;
import org.finos.waltz.model.actor.ImmutableActor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static org.finos.waltz.schema.Tables.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.Actor.ACTOR;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkOptionalIsPresent;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.StringUtilities.sanitizeCharacters;

@Repository
public class ActorDao {

    public static final org.finos.waltz.schema.tables.Actor actor = ACTOR.as("actor");

    public static final RecordMapper<Record, Actor> TO_DOMAIN_MAPPER = r -> {
        ActorRecord record = r.into(ActorRecord.class);

        return ImmutableActor.builder()
                .id(record.getId())
                .name(sanitizeCharacters(record.getName()))
                .description(sanitizeCharacters(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isExternal(record.getIsExternal())
                .provenance(record.getProvenance())
                .externalId(ofNullable(record.getExternalId()))
                .build();
    };



    private static final String PROVENANCE = "waltz";


    private final DSLContext dsl;


    @Autowired
    public ActorDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<Actor> findAll() {
        return dsl
                .select(actor.fields())
                .from(actor)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Actor getById(long id) {
        ActorRecord record = dsl
                .select(ACTOR.fields())
                .from(ACTOR)
                .where(ACTOR.ID.eq(id))
                .fetchOneInto(ActorRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Actor record with id: " + id);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public Long create(ActorCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        ActorRecord record = dsl.newRecord(ACTOR);
        record.setName(command.name());
        record.setDescription(command.description());
        record.setIsExternal(command.isExternal());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        command.externalId().ifPresent(record::setExternalId);
        record.setProvenance(PROVENANCE);
        record.store();

        return record.getId();
    }


    public boolean update(ActorChangeCommand command) {
        checkNotNull(command, "command cannot be null");
        checkOptionalIsPresent(command.lastUpdate(), "lastUpdate must be present");

        ActorRecord record = new ActorRecord();
        record.setId(command.id());
        record.changed(ACTOR.ID, false);

        command.name().ifPresent(change -> record.setName(change.newVal()));
        command.description().ifPresent(change -> record.setDescription(change.newVal()));
        command.isExternal().ifPresent(change -> record.setIsExternal(change.newVal()));
        command.externalId().ifPresent(change -> record.setExternalId(change.newVal()));

        command.lastUpdate().ifPresent(ts -> {
            record.setLastUpdatedAt(Timestamp.valueOf(ts.at()));
            record.setLastUpdatedBy(ts.by());
        });

        return dsl.executeUpdate(record) == 1;
    }


    public boolean deleteIfNotUsed(long id) {
        Condition notMentionedInFlows = DSL
                .notExists(DSL
                        .select(LOGICAL_FLOW.ID)
                        .from(LOGICAL_FLOW)
                        .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(id)
                            .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.ACTOR.name()))
                            .or(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(id)
                                    .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name())))));

        Condition notMentionedInInvolvements = DSL
                .notExists(DSL
                        .select(INVOLVEMENT.fields())
                        .from(INVOLVEMENT)
                        .where(INVOLVEMENT.ENTITY_ID.eq(id))
                        .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.ACTOR.name())));

        return dsl
                .deleteFrom(ACTOR)
                .where(ACTOR.ID.eq(id))
                .and(notMentionedInInvolvements)
                .and(notMentionedInFlows)
                .execute() > 0;
    }


}
