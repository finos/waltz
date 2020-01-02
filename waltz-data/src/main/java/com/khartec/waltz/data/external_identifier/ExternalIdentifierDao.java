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

package com.khartec.waltz.data.external_identifier;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.external_identifier.ImmutableExternalIdentifier;
import com.khartec.waltz.schema.tables.records.ExternalIdentifierRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.schema.tables.ExternalIdentifier.EXTERNAL_IDENTIFIER;

@Repository
public class ExternalIdentifierDao {

    private final DSLContext dsl;


    private static final Function<ExternalIdentifier, ExternalIdentifierRecord> TO_RECORD_MAPPER = d -> {
        ExternalIdentifierRecord record = new ExternalIdentifierRecord();
        record.setEntityId(d.entityReference().id());
        record.setEntityKind(d.entityReference().kind().name());
        record.setExternalId(d.externalId());
        record.setSystem(d.system());
        return record;
    };


    private static final RecordMapper<Record, ExternalIdentifier> TO_DOMAIN_MAPPER = r -> {
        ExternalIdentifierRecord record = r.into(EXTERNAL_IDENTIFIER);
        return ImmutableExternalIdentifier.builder()
                .externalId(record.getExternalId())
                .entityReference(JooqUtilities.readRef(record, EXTERNAL_IDENTIFIER.ENTITY_KIND, EXTERNAL_IDENTIFIER.ENTITY_ID))
                .system(record.getSystem())
                .build();
    };


    @Autowired
    public ExternalIdentifierDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public int[] create(Set<ExternalIdentifier> externalIdentifiers) {
        return dsl
                .batchInsert(map(externalIdentifiers, TO_RECORD_MAPPER))
                .execute();
    }


    public int create(ExternalIdentifier externalIdentifier) {
        return dsl.executeInsert(TO_RECORD_MAPPER.apply(externalIdentifier));
    }


    public Set<ExternalIdentifier> findByEntityReference(EntityReference entityRef) {
        return dsl
                .select(EXTERNAL_IDENTIFIER.fields())
                .from(EXTERNAL_IDENTIFIER)
                .where(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(entityRef.id()))
                .and(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(entityRef.kind().name()))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Set<ExternalIdentifier> findByKind(EntityKind kind, String extId) {
        return dsl
                .select(EXTERNAL_IDENTIFIER.fields())
                .from(EXTERNAL_IDENTIFIER)
                .where(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(kind.name()))
                .and(EXTERNAL_IDENTIFIER.EXTERNAL_ID.eq(extId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public int[] delete(Collection<ExternalIdentifier> externalIdentifiers) {
        return dsl
                .batchDelete(map(externalIdentifiers, TO_RECORD_MAPPER))
                .execute();
    }


    public int delete(ExternalIdentifier externalIdentifier) {
        return dsl.executeDelete(TO_RECORD_MAPPER.apply(externalIdentifier));
    }

    public int delete(EntityReference entityRef) {
        return dsl.deleteFrom(EXTERNAL_IDENTIFIER)
                .where(EXTERNAL_IDENTIFIER.ENTITY_KIND.eq(entityRef.kind().name()))
                .and(EXTERNAL_IDENTIFIER.ENTITY_ID.eq(entityRef.id()))
                .execute();
    }
}
