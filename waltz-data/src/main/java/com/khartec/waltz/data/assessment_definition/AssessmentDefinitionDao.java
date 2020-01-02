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

package com.khartec.waltz.data.assessment_definition;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_definition.AssessmentVisibility;
import com.khartec.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import com.khartec.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.AssessmentDefinition.ASSESSMENT_DEFINITION;


@Repository
public class AssessmentDefinitionDao {

    private static final RecordMapper<? super Record, AssessmentDefinition> TO_DOMAIN = r -> {
        AssessmentDefinitionRecord record = r.into(ASSESSMENT_DEFINITION);
        return ImmutableAssessmentDefinition.builder()
                .id(record.getId())
                .name(record.getName())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .ratingSchemeId(record.getRatingSchemeId())
                .entityKind(EntityKind.valueOf(record.getEntityKind()))
                .description(mkSafe(record.getDescription()))
                .permittedRole(Optional.ofNullable(record.getPermittedRole()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isReadOnly(record.getIsReadonly())
                .provenance(record.getProvenance())
                .visibility(AssessmentVisibility.PRIMARY.valueOf(record.getVisibility()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public AssessmentDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public AssessmentDefinition getById(long id) {
        return dsl.selectFrom(ASSESSMENT_DEFINITION)
                .where(ASSESSMENT_DEFINITION.ID.eq(id))
                .fetchOne(TO_DOMAIN);
    }


    public List<AssessmentDefinition> findAll() {
        return dsl.selectFrom(ASSESSMENT_DEFINITION)
                .fetch(TO_DOMAIN);
    }


    public List<AssessmentDefinition> findByEntityKind(EntityKind kind) {
        return dsl.selectFrom(ASSESSMENT_DEFINITION)
                .where(ASSESSMENT_DEFINITION.ENTITY_KIND.eq(kind.name()))
                .fetch(TO_DOMAIN);
    }

}
