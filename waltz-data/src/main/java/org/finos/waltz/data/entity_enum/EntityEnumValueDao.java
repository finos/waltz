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

package org.finos.waltz.data.entity_enum;

import org.finos.waltz.schema.tables.records.EntityEnumValueRecord;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_enum.EntityEnumValue;
import org.finos.waltz.model.entity_enum.ImmutableEntityEnumValue;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.schema.tables.EntityEnumValue.ENTITY_ENUM_VALUE;
import static org.finos.waltz.model.EntityReference.mkRef;

@Repository
public class EntityEnumValueDao {

    public static final RecordMapper<? super Record, EntityEnumValue> TO_DOMAIN_MAPPER = r -> {
        EntityEnumValueRecord record = r.into(ENTITY_ENUM_VALUE);

        return ImmutableEntityEnumValue.builder()
                .definitionId(record.getDefinitionId())
                .entityReference(mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .enumValueKey(record.getEnumValueKey())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityEnumValueDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityEnumValue> findByEntity(EntityReference ref) {
        return dsl
                .select(ENTITY_ENUM_VALUE.fields())
                .from(ENTITY_ENUM_VALUE)
                .where(ENTITY_ENUM_VALUE.ENTITY_ID.eq(ref.id()))
                .and(ENTITY_ENUM_VALUE.ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
