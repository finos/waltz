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

package com.khartec.waltz.data.rel;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.rel.ImmutableRelationshipKind;
import com.khartec.waltz.model.rel.RelationshipKind;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.records.RelationshipKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class RelationshipKindDao {

    private static final RecordMapper<Record, RelationshipKind> TO_DOMAIN_MAPPER = r -> {
        RelationshipKindRecord record = r.into(RelationshipKindRecord.class);
        return ImmutableRelationshipKind
                .builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .isReadOnly(record.getIsReadonly())
                .kindA(EntityKind.valueOf(record.getKindA()))
                .kindB(EntityKind.valueOf(record.getKindB()))
                .categoryA(record.getCategoryA())
                .categoryB(record.getCategoryB())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public RelationshipKindDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<RelationshipKind> findAll() {
        return dsl
                .selectFrom(Tables.RELATIONSHIP_KIND)
                .fetchSet(TO_DOMAIN_MAPPER);
    }

}
