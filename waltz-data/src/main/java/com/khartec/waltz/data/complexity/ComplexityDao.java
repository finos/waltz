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

package com.khartec.waltz.data.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.complexity.Complexity;
import com.khartec.waltz.model.complexity.ImmutableComplexity;
import com.khartec.waltz.schema.tables.records.ComplexityRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.COMPLEXITY;

@Repository
public class ComplexityDao {

    private static final RecordMapper<Record, Complexity> TO_COMPLEXITY_MAPPER = r -> {
        ComplexityRecord record = r.into(COMPLEXITY);
        return ImmutableComplexity.builder()
                .id(record.getId())
                .complexityKindId(record.getComplexityKindId())
                .entityReference(mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .score(record.getScore())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public ComplexityDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Set<Complexity> findByEntityReference(EntityReference ref){
        return dsl
                .selectFrom(COMPLEXITY)
                .where(COMPLEXITY.ENTITY_ID.eq(ref.id())
                        .and(COMPLEXITY.ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_COMPLEXITY_MAPPER);
    }
}