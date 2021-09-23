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
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.model.complexity.ComplexityKind;
import com.khartec.waltz.model.complexity.ImmutableComplexityKind;
import com.khartec.waltz.schema.tables.records.ComplexityKindRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.schema.Tables.COMPLEXITY;
import static com.khartec.waltz.schema.Tables.COMPLEXITY_KIND;

@Repository
public class ComplexityKindDao {

    public static final RecordMapper<Record, ComplexityKind> TO_COMPLEXITY_KIND_MAPPER = r -> {
        ComplexityKindRecord record = r.into(COMPLEXITY_KIND);
        return ImmutableComplexityKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(record.getExternalId())
                .isDefault(record.getIsDefault())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public ComplexityKindDao(DSLContext dsl) {
        Checks.checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Set<ComplexityKind> findAll(){
        return dsl
                .select(COMPLEXITY_KIND.fields())
                .from(COMPLEXITY_KIND)
                .fetchSet(TO_COMPLEXITY_KIND_MAPPER);
    }


    public ComplexityKind getById(Long complexityKindId) {
        return dsl
                .select(COMPLEXITY_KIND.fields())
                .from(COMPLEXITY_KIND)
                .where(COMPLEXITY_KIND.ID.eq(complexityKindId))
                .fetchOne(TO_COMPLEXITY_KIND_MAPPER);
    }


    public Set<ComplexityKind> findBySelector(GenericSelector genericSelector) {
        return dsl
                .select(COMPLEXITY_KIND.fields())
                .from(COMPLEXITY_KIND)
                .innerJoin(COMPLEXITY).on(COMPLEXITY_KIND.ID.eq(COMPLEXITY.COMPLEXITY_KIND_ID))
                .where(COMPLEXITY.ENTITY_ID.in(genericSelector.selector())
                        .and(COMPLEXITY.ENTITY_KIND.eq(genericSelector.kind().name())))
                .fetchSet(TO_COMPLEXITY_KIND_MAPPER);
    }
}