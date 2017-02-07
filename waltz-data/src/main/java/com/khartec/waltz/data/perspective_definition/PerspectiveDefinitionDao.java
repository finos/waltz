/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.data.perspective_definition;

import com.khartec.waltz.model.perspective.ImmutablePerspectiveDefinition;
import com.khartec.waltz.model.perspective.PerspectiveDefinition;
import com.khartec.waltz.model.rating.ImmutableRagNames;
import com.khartec.waltz.model.rating.RagNames;
import com.khartec.waltz.schema.tables.records.PerspectiveDefinitionRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PerspectiveDefinition.PERSPECTIVE_DEFINITION;

@Repository
public class PerspectiveDefinitionDao {

    private static final RecordMapper<PerspectiveDefinitionRecord, PerspectiveDefinition> TO_DOMAIN_MAPPER = r -> {
        RagNames ragNames = ImmutableRagNames.builder()
                .R(r.getRatingNameR())
                .A(r.getRatingNameA())
                .G(r.getRatingNameG())
                .Z(r.getRatingNameZ())
                .X(r.getRatingNameX())
                .build();

        return ImmutablePerspectiveDefinition.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .categoryX(r.getCategoryX())
                .categoryY(r.getCategoryY())
                .ragNames(ragNames)
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public PerspectiveDefinitionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<PerspectiveDefinition> findAll() {
        return dsl.selectFrom(PERSPECTIVE_DEFINITION)
                .orderBy(PERSPECTIVE_DEFINITION.NAME.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
