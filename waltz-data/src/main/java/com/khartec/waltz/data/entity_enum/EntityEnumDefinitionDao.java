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

package com.khartec.waltz.data.entity_enum;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_enum.EntityEnumDefinition;
import com.khartec.waltz.model.entity_enum.ImmutableEntityEnumDefinition;
import com.khartec.waltz.schema.tables.records.EntityEnumDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.EntityEnumDefinition.ENTITY_ENUM_DEFINITION;

@Repository
public class EntityEnumDefinitionDao {

    public static final RecordMapper<? super Record, EntityEnumDefinition> TO_DOMAIN_MAPPER = r -> {
        EntityEnumDefinitionRecord record = r.into(ENTITY_ENUM_DEFINITION);

        return ImmutableEntityEnumDefinition.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .entityKind(EntityKind.valueOf(record.getEntityKind()))
                .icon(record.getIconName())
                .enumValueType(record.getEnumValueType())
                .position(record.getPosition())
                .isEditable(record.getIsEditable())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityEnumDefinitionDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<EntityEnumDefinition> findByEntityKind(EntityKind kind) {
        return dsl.selectFrom(ENTITY_ENUM_DEFINITION)
                .where(ENTITY_ENUM_DEFINITION.ENTITY_KIND.eq(kind.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
