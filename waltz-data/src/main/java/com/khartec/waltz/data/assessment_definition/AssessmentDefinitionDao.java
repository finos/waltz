/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.assessment_definition;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import com.khartec.waltz.model.user.Role;
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
import static com.khartec.waltz.common.EnumUtilities.readEnum;
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
                .permittedRole(Optional.ofNullable(readEnum(record.getPermittedRole(), Role.class, s -> null)))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .isReadOnly(record.getIsReadonly())
                .provenance(record.getProvenance())
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
